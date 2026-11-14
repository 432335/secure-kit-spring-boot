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
package io.github.user432335.securekit.audit.properties;

import io.github.user432335.securekit.audit.autoconfigure.AuditAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AuditPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    AuditAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void hasExpectedDefaults() {
        runner.run(context -> {
            AuditProperties properties = context.getBean(AuditProperties.class);
            assertThat(properties.isEnabled()).isFalse();
            assertThat(properties.isAsync()).isTrue();
            assertThat(properties.getDefaultOperator()).isEqualTo("anonymous");
            assertThat(properties.isMaskBeforeSave()).isTrue();
            assertThat(properties.getAsyncCorePoolSize()).isEqualTo(1);
            assertThat(properties.getAsyncMaxPoolSize()).isEqualTo(4);
            assertThat(properties.getAsyncQueueCapacity()).isEqualTo(256);
        });
    }

    @Test
    void bindsAllProperties() {
        runner.withPropertyValues(
                        "secure-kit.audit.enabled=true",
                        "secure-kit.audit.async=false",
                        "secure-kit.audit.default-operator=alice",
                        "secure-kit.audit.mask-before-save=false",
                        "secure-kit.audit.async-core-pool-size=2",
                        "secure-kit.audit.async-max-pool-size=8",
                        "secure-kit.audit.async-queue-capacity=64")
                .run(context -> {
                    AuditProperties properties = context.getBean(AuditProperties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.isAsync()).isFalse();
                    assertThat(properties.getDefaultOperator()).isEqualTo("alice");
                    assertThat(properties.isMaskBeforeSave()).isFalse();
                    assertThat(properties.getAsyncCorePoolSize()).isEqualTo(2);
                    assertThat(properties.getAsyncMaxPoolSize()).isEqualTo(8);
                    assertThat(properties.getAsyncQueueCapacity()).isEqualTo(64);
                });
    }

    @Test
    void mapsExecutorParameters() {
        runner.withPropertyValues(
                        "secure-kit.audit.enabled=true",
                        "secure-kit.audit.async-core-pool-size=2",
                        "secure-kit.audit.async-max-pool-size=6",
                        "secure-kit.audit.async-queue-capacity=32")
                .run(context -> {
                    ThreadPoolTaskExecutor executor = context.getBean(
                            "auditTaskExecutor", ThreadPoolTaskExecutor.class);
                    assertThat(executor.getCorePoolSize()).isEqualTo(2);
                    assertThat(executor.getMaxPoolSize()).isEqualTo(6);
                    assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity())
                            .isEqualTo(32);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AuditProperties.class)
    static class PropertiesConfiguration {
    }
}
