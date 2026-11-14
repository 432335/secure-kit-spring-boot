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

import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zero-persistence default audit repository.
 *
 * <p>This implementation writes the event summary at DEBUG level and stores nothing. It exists so
 * the audit capability can be enabled for local evaluation without a database or message broker.
 * Production applications should replace it with their own thread-safe {@link AuditRepository}
 * bean.</p>
 *
 * <p>This class is stateless and safe for concurrent use.</p>
 */
public class Slf4jAuditRepository implements AuditRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jAuditRepository.class);

    /**
     * Writes the event summary at DEBUG level.
     *
     * @param event the event to write; {@code null} is ignored
     */
    @Override
    public void save(AuditEvent event) {
        if (event == null) {
            return;
        }
        LOGGER.debug("secure-kit-audit event: {}", event);
    }
}
