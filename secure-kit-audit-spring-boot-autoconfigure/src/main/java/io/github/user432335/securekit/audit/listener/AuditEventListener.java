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
package io.github.user432335.securekit.audit.listener;

import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import io.github.user432335.securekit.core.MaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Receives audit events and hands them to an {@link AuditRepository}.
 *
 * <p>The listener is an optional collaboration point. It does not depend on the mask module at
 * compile time. At runtime it scans injected beans for a bean whose user class is
 * {@code io.github.user432335.securekit.mask.core.MaskTemplate}; when found, operator, target and summary
 * fields are masked before saving. When it is absent, the event is stored as plaintext and one
 * WARN message is emitted for the lifetime of this listener.</p>
 *
 * <p>All listener failures are caught and logged at DEBUG level. An exception never escapes into
 * the async executor or into the business thread.</p>
 *
 * <p>This class is safe for concurrent use. The warning guard is atomic and the masker lookup is
 * read-only.</p>
 */
public class AuditEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventListener.class);

    private static final String MASK_TEMPLATE_CLASS_NAME =
            "io.github.user432335.securekit.mask.core.MaskTemplate";
    private static final String MASK_WARNING =
            "secure-kit-audit: mask module not found, audit events will be stored in plaintext. "
                    + "Add secure-kit-mask-spring-boot-starter for automatic masking.";
    private static final MaskType[] MASK_TYPES = {
            MaskType.PHONE,
            MaskType.ID_CARD,
            MaskType.BANK_CARD,
            MaskType.EMAIL
    };
    private static final List<Pattern> FALLBACK_PATTERNS = List.of(
            Pattern.compile("\\d{13,19}"),
            Pattern.compile("\\d{17}[\\dXx]"),
            Pattern.compile("1[3-9]\\d{9}"),
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    );
    private static final Method MASK_METHOD = resolveMaskMethod("mask", MaskType.class, String.class);
    private static final Method MASK_PATTERN_METHOD = resolveMaskMethod(
            "mask", MaskType.class, String.class, String.class, String.class);

    private final AuditRepository repository;
    private final AuditProperties properties;
    private final ObjectProvider<Object> beanProvider;
    private final AtomicBoolean warningEmitted = new AtomicBoolean(false);

    /**
     * Creates the listener.
     *
     * @param repository   the persistence boundary; must not be {@code null}
     * @param properties   the audit properties; must not be {@code null}
     * @param beanProvider a provider used to look for an optional mask-template bean; may be
     *                     {@code null} in tests, in which case the listener degrades to plaintext
     * @throws NullPointerException when {@code repository} or {@code properties} is {@code null}
     */
    public AuditEventListener(AuditRepository repository,
                              AuditProperties properties,
                              ObjectProvider<Object> beanProvider) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.beanProvider = beanProvider;
    }

    /**
     * Handles an audit event.
     *
     * <p>When pre-save masking is enabled and the mask module is present, a masked copy of the
     * event is saved. Otherwise the original event is saved and the degradation warning is emitted
     * once. Exceptions are caught and logged at DEBUG.</p>
     *
     * @param event the event; {@code null} is ignored
     */
    @Async("auditTaskExecutor")
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        if (event == null) {
            return;
        }
        try {
            AuditEvent eventToSave = event;
            if (properties.isMaskBeforeSave()) {
                Object maskTemplate = findMaskTemplate();
                if (maskTemplate != null && MASK_METHOD != null) {
                    eventToSave = event.masked(value -> maskText(maskTemplate, value));
                } else {
                    warnOnce();
                }
            }
            repository.save(eventToSave);
        } catch (Throwable throwable) {
            LOGGER.debug("secure-kit-audit: failed to process audit event", throwable);
        }
    }

    private Object findMaskTemplate() {
        if (beanProvider == null) {
            return null;
        }
        try {
            return beanProvider.orderedStream()
                    .filter(Objects::nonNull)
                    .filter(bean -> MASK_TEMPLATE_CLASS_NAME.equals(
                            ClassUtils.getUserClass(bean).getName()))
                    .findFirst()
                    .orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String maskText(Object maskTemplate, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String current = value;
        for (MaskType type : MASK_TYPES) {
            try {
                Object masked = MASK_METHOD.invoke(maskTemplate, type, current);
                if (masked instanceof String maskedText) {
                    current = maskedText;
                }
            } catch (Throwable throwable) {
                LOGGER.debug("secure-kit-audit: mask invocation failed for type {}", type, throwable);
            }
        }
        if (MASK_PATTERN_METHOD != null) {
            for (Pattern pattern : FALLBACK_PATTERNS) {
                try {
                    Object masked = MASK_PATTERN_METHOD.invoke(
                            maskTemplate, MaskType.CUSTOM, current, pattern.pattern(), null);
                    if (masked instanceof String maskedText) {
                        current = maskedText;
                    }
                } catch (Throwable throwable) {
                    LOGGER.debug("secure-kit-audit: fallback mask invocation failed", throwable);
                }
            }
        }
        return current;
    }

    private void warnOnce() {
        if (warningEmitted.compareAndSet(false, true)) {
            LOGGER.warn(MASK_WARNING);
        }
    }

    private static Method resolveMaskMethod(String name, Class<?>... parameterTypes) {
        try {
            Class<?> type = Class.forName(MASK_TEMPLATE_CLASS_NAME, false,
                    AuditEventListener.class.getClassLoader());
            return type.getMethod(name, parameterTypes);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
