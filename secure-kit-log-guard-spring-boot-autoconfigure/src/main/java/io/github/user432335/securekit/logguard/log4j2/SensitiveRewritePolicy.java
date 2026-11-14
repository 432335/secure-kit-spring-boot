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
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.StringMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Log4j2 rewrite policy that guards the formatted message and context data.
 *
 * <p>Log4j2 instantiates rewrite policies without a Spring context. The no-argument constructor
 * therefore uses a {@link SensitivePatternSet} backed by system properties and built-in defaults.
 * See {@link SensitivePatternSet} for the supported system properties.</p>
 *
 * <p>Example {@code log4j2.xml} configuration:</p>
 * <pre>{@code
 * <Configuration status="WARN">
 *   <Appenders>
 *     <Console name="Console" target="SYSTEM_OUT">
 *       <PatternLayout pattern="%d %-5p %c - %m%n"/>
 *     </Console>
 *     <Rewrite name="Rewrite">
 *       <SensitiveRewritePolicy/>
 *       <AppenderRef ref="Console"/>
 *     </Rewrite>
 *   </Appenders>
 *   <Loggers>
 *     <Root level="info">
 *       <AppenderRef ref="Rewrite"/>
 *     </Root>
 *   </Loggers>
 * </Configuration>
 * }</pre>
 *
 * <p>The library never edits {@code log4j2.xml}; the application must add the rewrite policy and
 * reference it from an appender. The policy rebuilds the event with guarded message and context
 * data. If rebuilding fails, the original event is returned so logging continues.</p>
 *
 * <p>This class is stateless after construction and safe for concurrent use.</p>
 */
public class SensitiveRewritePolicy implements RewritePolicy {

    private final SensitivePatternSet patternSet;

    /**
     * Creates a policy using the system-property/default pattern set.
     */
    public SensitiveRewritePolicy() {
        this(new SensitivePatternSet());
    }

    SensitiveRewritePolicy(SensitivePatternSet patternSet) {
        this.patternSet = patternSet;
    }

    /**
     * Rewrites an event with guarded message and context data.
     *
     * @param source the source event; may be {@code null}
     * @return the rewritten event, the original event when rewriting fails, or {@code null} for a
     *         {@code null} input
     */
    @Override
    public LogEvent rewrite(LogEvent source) {
        if (source == null) {
            return null;
        }
        try {
            String message = source.getMessage() == null
                    ? ""
                    : source.getMessage().getFormattedMessage();
            String guardedMessage = patternSet.apply(message);
            StringMap guardedContextData = guardContextData(source);
            return new Log4jLogEvent.Builder(source)
                    .setMessage(new SimpleMessage(guardedMessage))
                    .setContextData(guardedContextData)
                    .build();
        } catch (RuntimeException ignored) {
            return source;
        }
    }

    private StringMap guardContextData(LogEvent source) {
        Map<String, String> guarded = new LinkedHashMap<>();
        if (source.getContextData() != null) {
            source.getContextData().toMap().forEach(
                    (key, value) -> guarded.put(key, patternSet.apply(value)));
        }
        return ContextDataFactory.createContextData(guarded);
    }
}
