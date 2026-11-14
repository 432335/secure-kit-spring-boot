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
package io.github.user432335.securekit.audit.autoconfigure;

import io.github.user432335.securekit.audit.aop.AuditAspect;
import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.listener.AuditEventListener;
import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import io.github.user432335.securekit.audit.spi.AuditUserProvider;
import io.github.user432335.securekit.audit.support.DefaultAuditUserProvider;
import io.github.user432335.securekit.audit.support.Slf4jAuditRepository;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AuditAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

    @Test
    void isDisabledByDefault() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(AuditAspect.class);
            assertThat(context).doesNotHaveBean(AuditEventListener.class);
            assertThat(context).doesNotHaveBean(AuditRepository.class);
            assertThat(context).doesNotHaveBean(AuditUserProvider.class);
            assertThat(context).doesNotHaveBean("auditTaskExecutor");
        });
    }

    @Test
    void registersBeansWhenEnabled() {
        runner.withPropertyValues("secure-kit.audit.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(AuditAspect.class);
            assertThat(context).hasSingleBean(AuditEventListener.class);
            assertThat(context).hasSingleBean(AuditRepository.class);
            assertThat(context).hasSingleBean(AuditUserProvider.class);
            assertThat(context.getBean(AuditRepository.class)).isInstanceOf(Slf4jAuditRepository.class);
            assertThat(context.getBean(AuditUserProvider.class)).isInstanceOf(DefaultAuditUserProvider.class);
            assertThat(context).hasBean("auditTaskExecutor");
        });
    }

    @Test
    void configuresBoundedDaemonExecutor() throws Exception {
        runner.withPropertyValues(
                        "secure-kit.audit.enabled=true",
                        "secure-kit.audit.async-core-pool-size=1",
                        "secure-kit.audit.async-max-pool-size=2",
                        "secure-kit.audit.async-queue-capacity=7")
                .run(context -> {
                    ThreadPoolTaskExecutor executor = context.getBean(
                            "auditTaskExecutor", ThreadPoolTaskExecutor.class);
                    ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
                    assertThat(pool.getCorePoolSize()).isEqualTo(1);
                    assertThat(pool.getMaximumPoolSize()).isEqualTo(2);
                    assertThat(pool.getQueue().remainingCapacity()).isEqualTo(7);

                    RejectedExecutionHandler handler = pool.getRejectedExecutionHandler();
                    assertThat(handler).isNotInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
                    assertThatCode(() -> handler.rejectedExecution(() -> { }, pool))
                            .doesNotThrowAnyException();

                    CountDownLatch latch = new CountDownLatch(1);
                    AtomicReference<String> threadName = new AtomicReference<>();
                    AtomicBoolean daemon = new AtomicBoolean(false);
                    executor.execute(() -> {
                        threadName.set(Thread.currentThread().getName());
                        daemon.set(Thread.currentThread().isDaemon());
                        latch.countDown();
                    });
                    assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
                    assertThat(threadName.get()).startsWith("secure-kit-audit-");
                    assertThat(daemon.get()).isTrue();
                });
    }

    @Test
    void backsOffForUserDefinedRepository() {
        runner.withPropertyValues("secure-kit.audit.enabled=true")
                .withUserConfiguration(CustomRepositoryConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditRepository.class);
                    assertThat(context.getBean(AuditRepository.class))
                            .isInstanceOf(CapturingRepository.class);
                });
    }

    @Test
    void backsOffForUserDefinedUserProvider() {
        runner.withPropertyValues("secure-kit.audit.enabled=true")
                .withUserConfiguration(CustomUserProviderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditUserProvider.class);
                    assertThat(context.getBean(AuditUserProvider.class).currentOperator())
                            .isEqualTo("custom-user");
                });
    }

    @Test
    void listenerStillAssemblesWhenMaskClassIsFiltered() {
        runner.withPropertyValues("secure-kit.audit.enabled=true")
                .withClassLoader(new FilteredClassLoader("io.github.user432335.securekit.mask.core.MaskTemplate"))
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditEventListener.class);
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    void asyncFalseDoesNotCreateAnAsyncProxy() {
        runner.withPropertyValues(
                        "secure-kit.audit.enabled=true",
                        "secure-kit.audit.async=false")
                .run(context -> {
                    AuditEventListener listener = context.getBean(AuditEventListener.class);
                    assertThat(AopUtils.isAopProxy(listener)).isFalse();
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomRepositoryConfiguration {

        @Bean
        AuditRepository customAuditRepository() {
            return new CapturingRepository();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomUserProviderConfiguration {

        @Bean
        AuditUserProvider customAuditUserProvider() {
            return () -> "custom-user";
        }
    }

    static class CapturingRepository implements AuditRepository {

        @Override
        public void save(AuditEvent event) {
            // No-op test double.
        }
    }
}
