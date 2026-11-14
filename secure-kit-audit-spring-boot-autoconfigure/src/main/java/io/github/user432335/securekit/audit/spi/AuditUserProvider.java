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
package io.github.user432335.securekit.audit.spi;

/**
 * Supplies the current operator name for audit events.
 *
 * <p>Implementations may read a Spring Security context, MDC, a request-scoped bean, a
 * ThreadLocal, or any application-specific source. The audit module does not depend on any
 * authentication or authorization library.</p>
 *
 * <p><strong>Implementations must be thread-safe.</strong> The method may be called from any
 * business thread. Returning {@code null} or an empty value causes the listener/aspect to use the
 * configured default operator.</p>
 */
public interface AuditUserProvider {

    /**
     * Returns the current operator name.
     *
     * @return the operator name; may be {@code null}
     */
    String currentOperator();
}
