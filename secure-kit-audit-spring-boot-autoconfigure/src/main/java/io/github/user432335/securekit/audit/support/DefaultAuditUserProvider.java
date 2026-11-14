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
package io.github.user432335.securekit.audit.support;

import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditUserProvider;

import java.util.Objects;

/**
 * Default operator provider backed by configuration.
 *
 * <p>It returns {@code secure-kit.audit.default-operator}, falling back to {@code "anonymous"}.
 * Applications that have an authenticated principal should replace this bean with their own
 * thread-safe {@link AuditUserProvider}.</p>
 *
 * <p>This class is immutable and safe for concurrent use.</p>
 */
public class DefaultAuditUserProvider implements AuditUserProvider {

    private static final String FALLBACK_OPERATOR = "anonymous";

    private final AuditProperties properties;

    /**
     * Creates the provider.
     *
     * @param properties the audit properties; must not be {@code null}
     * @throws NullPointerException when {@code properties} is {@code null}
     */
    public DefaultAuditUserProvider(AuditProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    /**
     * Returns the configured default operator.
     *
     * @return the configured operator, or {@code "anonymous"} when it is {@code null} or blank
     */
    @Override
    public String currentOperator() {
        String configured = properties.getDefaultOperator();
        return configured == null || configured.isBlank() ? FALLBACK_OPERATOR : configured;
    }
}
