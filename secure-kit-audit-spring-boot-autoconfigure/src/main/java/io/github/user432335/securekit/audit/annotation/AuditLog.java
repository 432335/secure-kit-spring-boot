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
package io.github.user432335.securekit.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method whose invocation should produce an audit event.
 *
 * <p>The audit capability uses an AOP notification point only as an implementation detail. It does
 * not change the method's return value, does not swallow exceptions and does not block business
 * execution. If the audit notification itself fails, the failure is ignored and the business call
 * continues normally.</p>
 *
 * <p>This annotation is a capability opt-in. The audit module is disabled by default; even when it
 * is enabled, only methods carrying this annotation are observed.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * Human-readable description of the audited action.
     *
     * @return the action description, never {@code null}
     */
    String action();

    /**
     * Optional Spring Expression Language expression used to derive a target identifier.
     *
     * <p>Examples are {@code "#p0.userId"} and {@code "#result.id"}. An empty value skips target
     * identifier evaluation. Evaluation failures are ignored and produce a {@code null} target
     * identifier; they never affect the business method.</p>
     *
     * @return the SpEL expression, or an empty string
     */
    String targetIdExpression() default "";
}
