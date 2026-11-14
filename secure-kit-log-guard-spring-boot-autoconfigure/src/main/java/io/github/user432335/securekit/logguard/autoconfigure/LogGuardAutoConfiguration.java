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
package io.github.user432335.securekit.logguard.autoconfigure;

import io.github.user432335.securekit.logguard.core.SensitivePatternSet;
import io.github.user432335.securekit.logguard.properties.LogGuardProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the log output guarding capability library.
 *
 * <p>This module is a logging-framework extension hook, not a framework that takes over
 * application logging. Use it in three steps:</p>
 * <ol>
 *     <li>Add {@code secure-kit-log-guard-spring-boot-starter} to the application.</li>
 *     <li>Set {@code secure-kit.log-guard.enabled=true}.</li>
 *     <li>Reference {@code SensitiveMessageConverter} from {@code logback.xml}, or
 *     {@code SensitiveRewritePolicy} from {@code log4j2.xml}, as documented on those classes.</li>
 * </ol>
 *
 * <p>The module does <strong>not</strong> automatically modify or generate the application's
 * logging configuration. It only registers a {@link SensitivePatternSet} bean for applications
 * that want to reuse the configured patterns. The logging frameworks create their own converter
 * or policy instances, which read the documented system properties.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(LogGuardProperties.class)
@ConditionalOnProperty(prefix = "secure-kit.log-guard", name = "enabled", havingValue = "true")
public class LogGuardAutoConfiguration {

    /**
     * Creates the immutable sensitive pattern set unless the application provides one.
     *
     * @param properties the log-guard properties
     * @return the pattern set
     */
    @Bean
    @ConditionalOnMissingBean
    public SensitivePatternSet sensitivePatternSet(LogGuardProperties properties) {
        return new SensitivePatternSet(properties);
    }
}
