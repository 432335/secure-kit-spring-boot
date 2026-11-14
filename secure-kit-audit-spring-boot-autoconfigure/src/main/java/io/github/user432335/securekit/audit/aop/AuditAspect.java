/*
 * Copyright 2026 the secure-kit-spring-boot authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.user432335.securekit.audit.aop;

import io.github.user432335.securekit.audit.annotation.AuditLog;
import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditUserProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Objects;

/**
 * Optional AOP notification point for {@link AuditLog}-annotated methods.
 *
 * <p>This aspect is an implementation detail of the audit capability library. It does not change
 * the method return value, does not swallow business exceptions and does not block business
 * execution. It runs at {@link Ordered#LOWEST_PRECEDENCE} so that business aspects can run first
 * and the audit observation stays at the edge.</p>
 *
 * <p>All audit-side failures (SpEL evaluation, event construction, publisher failure) are caught
 * and ignored. The original business result or exception is always preserved.</p>
 */
@Aspect
@org.springframework.core.annotation.Order(Ordered.LOWEST_PRECEDENCE)
public class AuditAspect {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();
    private static final String FALLBACK_OPERATOR = "anonymous";

    private final AuditUserProvider userProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditProperties properties;

    /**
     * Creates the audit aspect.
     *
     * @param userProvider   the operator provider; must not be {@code null}
     * @param eventPublisher the Spring event publisher; must not be {@code null}
     * @param properties     the audit properties; must not be {@code null}
     * @throws NullPointerException when any argument is {@code null}
     */
    public AuditAspect(AuditUserProvider userProvider,
                       ApplicationEventPublisher eventPublisher,
                       AuditProperties properties) {
        this.userProvider = Objects.requireNonNull(userProvider, "userProvider must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    /**
     * Observes an annotated method and publishes an audit event.
     *
     * <p>The business method is always invoked. If it throws, the same throwable is rethrown after
     * the failure event is published. Any failure inside the audit notification is ignored.</p>
     *
     * @param joinPoint the proceeding join point
     * @param auditLog  the annotation metadata
     * @return the original business result
     * @throws Throwable the original business throwable, unchanged
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Object result = null;
        Throwable businessFailure = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            businessFailure = throwable;
        }

        try {
            publishEvent(joinPoint, auditLog, result, businessFailure);
        } catch (Throwable ignored) {
            // Audit must never affect the business call. The original outcome is handled below.
        }

        if (businessFailure != null) {
            throw businessFailure;
        }
        return result;
    }

    private void publishEvent(ProceedingJoinPoint joinPoint,
                              AuditLog auditLog,
                              Object result,
                              Throwable businessFailure) {
        String operator = resolveOperator();
        String targetId = evaluateTargetId(joinPoint, auditLog, result);
        String action = auditLog == null ? "" : auditLog.action();
        String methodSignature = joinPoint.getSignature() == null
                ? ""
                : joinPoint.getSignature().toShortString();
        String errorMessage = businessFailure == null ? null : failureMessage(businessFailure);

        AuditEvent event = new AuditEvent(
                operator,
                action,
                targetId,
                methodSignature,
                joinPoint.getArgs(),
                result,
                businessFailure == null,
                errorMessage,
                Instant.now());
        eventPublisher.publishEvent(event);
    }

    private String resolveOperator() {
        try {
            String operator = userProvider.currentOperator();
            if (operator != null && !operator.isBlank()) {
                return operator;
            }
        } catch (RuntimeException ignored) {
            // Fall through to the configured default.
        }
        String configured = properties.getDefaultOperator();
        return configured == null || configured.isBlank() ? FALLBACK_OPERATOR : configured;
    }

    private String evaluateTargetId(ProceedingJoinPoint joinPoint, AuditLog auditLog, Object result) {
        if (auditLog == null || auditLog.targetIdExpression() == null
                || auditLog.targetIdExpression().isBlank()) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] arguments = joinPoint.getArgs() == null ? new Object[0] : joinPoint.getArgs();
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                    null, method, arguments, PARAMETER_NAME_DISCOVERER);
            for (int i = 0; i < arguments.length; i++) {
                context.setVariable("p" + i, arguments[i]);
                context.setVariable("a" + i, arguments[i]);
            }
            context.setVariable("result", result);
            Object value = EXPRESSION_PARSER.parseExpression(auditLog.targetIdExpression())
                    .getValue(context);
            return value == null ? null : String.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String failureMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return throwable.getClass().getName();
    }
}
