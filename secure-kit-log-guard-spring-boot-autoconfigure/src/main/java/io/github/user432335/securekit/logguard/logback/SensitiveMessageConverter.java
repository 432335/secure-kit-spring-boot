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
package io.github.user432335.securekit.logguard.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.user432335.securekit.logguard.core.SensitivePatternSet;

/**
 * Logback converter that guards the formatted log message.
 *
 * <p>Logback instantiates converters without a Spring context. The no-argument constructor
 * therefore uses a {@link SensitivePatternSet} backed by system properties and built-in defaults.
 * See {@link SensitivePatternSet} for the supported system properties.</p>
 *
 * <p>Example {@code logback.xml} configuration using a dedicated conversion word:</p>
 * <pre>{@code
 * <configuration>
 *   <conversionRule conversionWord="smsg"
 *                   converterClass="io.github.user432335.securekit.logguard.logback.SensitiveMessageConverter"/>
 *   <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 *     <encoder>
 *       <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %smsg%n</pattern>
 *     </encoder>
 *   </appender>
 *   <root level="INFO"><appender-ref ref="CONSOLE"/></root>
 * </configuration>
 * }</pre>
 *
 * <p>Using a dedicated conversion word such as {@code smsg} is recommended. Overriding the
 * built-in {@code msg} word also works, but it changes every message rendered by the logging
 * context and is therefore a global side effect:</p>
 * <pre>{@code
 * <conversionRule conversionWord="msg"
 *                 converterClass="io.github.user432335.securekit.logguard.logback.SensitiveMessageConverter"/>
 * }</pre>
 *
 * <p>MDC values are not automatically guarded by this converter. If MDC values are part of the
 * pattern, wrap them with a dedicated converter or mask them before placing them in MDC. The
 * {@link #maskMdcValue(String)} helper is available for application code that wants to do that.</p>
 *
 * <p>This class is stateless after construction and safe for concurrent use.</p>
 */
public class SensitiveMessageConverter extends ClassicConverter {

    private final SensitivePatternSet patternSet;

    /**
     * Creates a converter using the system-property/default pattern set.
     */
    public SensitiveMessageConverter() {
        this(new SensitivePatternSet());
    }

    SensitiveMessageConverter(SensitivePatternSet patternSet) {
        this.patternSet = patternSet;
    }

    /**
     * Returns the guarded formatted message.
     *
     * @param event the logging event; may be {@code null}
     * @return the guarded message, or an empty string for a {@code null} event
     */
    @Override
    public String convert(ILoggingEvent event) {
        if (event == null) {
            return "";
        }
        return patternSet.apply(event.getFormattedMessage());
    }

    /**
     * Guards one MDC value.
     *
     * <p>This method is a convenience for application code that stores MDC values. It does not
     * change how Logback renders {@code %X}; configure a dedicated converter if MDC output must be
     * guarded automatically.</p>
     *
     * @param value the MDC value; may be {@code null}
     * @return the guarded value, or {@code null} for a {@code null} input
     */
    public String maskMdcValue(String value) {
        return patternSet.apply(value);
    }
}
