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

import io.github.user432335.securekit.audit.event.AuditEvent;

/**
 * Persistence boundary for audit events.
 *
 * <p>The default implementation only writes a DEBUG line and performs no persistence. Production
 * applications should replace it with a bean that writes to their own store.</p>
 *
 * <p><strong>Implementations must be thread-safe.</strong> The listener may invoke this method
 * concurrently from the bounded audit executor. Implementations should return quickly and must
 * not throw for ordinary storage failures if they want the listener's best-effort semantics to
 * continue; the listener catches exceptions in any case.</p>
 */
public interface AuditRepository {

    /**
     * Saves an audit event.
     *
     * @param event the event to save; never {@code null}
     */
    void save(AuditEvent event);
}
