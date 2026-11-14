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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import io.github.user432335.securekit.logguard.core.SensitivePatternSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveMessageConverterTest {

    private final SensitiveMessageConverter converter = new SensitiveMessageConverter(
            new SensitivePatternSet(List.of("1[3-9]\\d{9}"), "#"));

    @Test
    void masksFormattedMessage() {
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event = new LoggingEvent(
                "test", logger, Level.INFO, "contact 13812345678", null, null);

        assertThat(converter.convert(event)).isEqualTo("contact #");
    }

    @Test
    void returnsEmptyForNullEvent() {
        assertThat(converter.convert(null)).isEmpty();
    }

    @Test
    void masksMdcValue() {
        assertThat(converter.maskMdcValue("13812345678")).isEqualTo("#");
        assertThat(converter.maskMdcValue(null)).isNull();
    }

    @Test
    void defaultConstructorUsesBuiltInPatterns() {
        SensitiveMessageConverter defaultConverter = new SensitiveMessageConverter();
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("test");
        LoggingEvent event = new LoggingEvent(
                "test", logger, Level.INFO, "password=secret", null, null);

        assertThat(defaultConverter.convert(event)).isEqualTo("*");
    }
}
