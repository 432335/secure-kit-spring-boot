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
import io.github.user432335.securekit.audit.listener.AuditEventListener;
import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import io.github.user432335.securekit.audit.spi.AuditUserProvider;
import io.github.user432335.securekit.audit.support.DefaultAuditUserProvider;
import io.github.user432335.securekit.audit.support.Slf4jAuditRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Auto-configuration for the audit capability library.
 *
 * <p>The capability is disabled by default and active only when
 * {@code secure-kit.audit.enabled=true}. Enabling it registers an optional AOP notification point,
 * an event listener, a default repository and a default operator provider. Every bean backs off
 * when the application defines its own bean of the same type.</p>
 *
 * <p>The audit executor has a bounded queue and a discard-and-count rejection policy. Rejection
 * never throws and therefore never affects a business thread. Its worker threads are daemon
 * threads with the {@code secure-kit-audit-} prefix, so audit work cannot keep the JVM alive.</p>
 *
 * <p>Asynchronous delivery is enabled only when {@code secure-kit.audit.async=true} (the default).
 * The listener also carries {@code @Async}; if the application enables async processing itself
 * while setting this property to {@code false}, the application's own async configuration takes
 * precedence. Set both consistently.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(AuditProperties.class)
@ConditionalOnProperty(prefix = "secure-kit.audit", name = "enabled", havingValue = "true")
public class AuditAutoConfiguration {

    /**
     * Creates the audit aspect unless the application provides one.
     *
     * @param userProvider   the operator provider
     * @param eventPublisher the Spring event publisher
     * @param properties     the audit properties
     * @return the aspect
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditAspect auditAspect(AuditUserProvider userProvider,
                                   org.springframework.context.ApplicationEventPublisher eventPublisher,
                                   AuditProperties properties) {
        return new AuditAspect(userProvider, eventPublisher, properties);
    }

    /**
     * Creates the audit listener unless the application provides one.
     *
     * @param repository   the persistence boundary
     * @param properties   the audit properties
     * @param beanProvider a provider used for optional mask-module detection
     * @return the listener
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditEventListener auditEventListener(AuditRepository repository,
                                                 AuditProperties properties,
                                                 ObjectProvider<Object> beanProvider) {
        return new AuditEventListener(repository, properties, beanProvider);
    }

    /**
     * Creates the zero-persistence default repository unless the application provides one.
     *
     * @return the default repository
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditRepository auditRepository() {
        return new Slf4jAuditRepository();
    }

    /**
     * Creates the configuration-backed operator provider unless the application provides one.
     *
     * @param properties the audit properties
     * @return the default operator provider
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditUserProvider auditUserProvider(AuditProperties properties) {
        return new DefaultAuditUserProvider(properties);
    }

    /**
     * Creates the discard-and-count rejection policy used by the audit executor.
     *
     * @return a policy that discards and counts without throwing
     */
    @Bean
    @ConditionalOnMissingBean(name = "auditRejectedExecutionHandler")
    public RejectedExecutionHandler auditRejectedExecutionHandler() {
        return new CountingDiscardPolicy();
    }

    /**
     * Creates the bounded audit executor unless the application provides one.
     *
     * @param properties      the audit properties
     * @param rejectionHandler the discard-and-count policy
     * @return the configured executor
     */
    @Bean(name = "auditTaskExecutor")
    @ConditionalOnMissingBean(name = "auditTaskExecutor")
    public ThreadPoolTaskExecutor auditTaskExecutor(
            AuditProperties properties,
            @Qualifier("auditRejectedExecutionHandler") RejectedExecutionHandler rejectionHandler) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getAsyncCorePoolSize());
        executor.setMaxPoolSize(properties.getAsyncMaxPoolSize());
        executor.setQueueCapacity(properties.getAsyncQueueCapacity());
        executor.setThreadNamePrefix("secure-kit-audit-");
        executor.setDaemon(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.setRejectedExecutionHandler(rejectionHandler);
        return executor;
    }

    /**
     * Enables asynchronous event delivery only when {@code secure-kit.audit.async=true}.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "secure-kit.audit", name = "async", havingValue = "true",
            matchIfMissing = true)
    @EnableAsync
    static class AsyncConfiguration {
    }

    /**
     * Rejection policy that silently discards work and increments a counter.
     *
     * <p>It deliberately never logs and never throws. Logging from a rejection handler can recurse
     * into the logging system while the executor is saturated.</p>
     */
    static final class CountingDiscardPolicy extends ThreadPoolExecutor.DiscardPolicy {

        private final AtomicLong rejectedCount = new AtomicLong();

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            rejectedCount.incrementAndGet();
        }

        long getRejectedCount() {
            return rejectedCount.get();
        }
    }
}
