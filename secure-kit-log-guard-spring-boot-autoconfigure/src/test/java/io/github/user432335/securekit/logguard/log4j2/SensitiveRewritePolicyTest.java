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
package io.github.user432335.securekit.logguard.log4j2;

import io.github.user432335.securekit.logguard.core.SensitivePatternSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.StringMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveRewritePolicyTest {

    private final SensitiveRewritePolicy policy = new SensitiveRewritePolicy(
            new SensitivePatternSet(List.of("1[3-9]\\d{9}"), "#"));

    @Test
    void rewritesMessageAndContextData() {
        StringMap contextData = ContextDataFactory.createContextData();
        contextData.putValue("phone", "13812345678");
        LogEvent source = Log4jLogEvent.newBuilder()
                .setLoggerName("test")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("contact 13812345678"))
                .setContextData(contextData)
                .build();

        LogEvent rewritten = policy.rewrite(source);

        assertThat(rewritten).isNotNull();
        assertThat(rewritten.getMessage().getFormattedMessage()).isEqualTo("contact #");
        assertThat((String) rewritten.getContextData().getValue("phone")).isEqualTo("#");
    }

    @Test
    void returnsNullForNullEvent() {
        assertThat(policy.rewrite((LogEvent) null)).isNull();
    }

    @Test
    void defaultConstructorUsesBuiltInPatterns() {
        SensitiveRewritePolicy defaultPolicy = new SensitiveRewritePolicy();
        LogEvent source = Log4jLogEvent.newBuilder()
                .setLoggerName("test")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("password=secret"))
                .build();

        LogEvent rewritten = defaultPolicy.rewrite(source);

        assertThat(rewritten.getMessage().getFormattedMessage()).isEqualTo("*");
    }
}
