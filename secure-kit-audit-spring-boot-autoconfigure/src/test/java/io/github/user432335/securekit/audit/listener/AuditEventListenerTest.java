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
package io.github.user432335.securekit.audit.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import io.github.user432335.securekit.mask.core.MaskTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AuditEventListenerTest {

    private AuditRepository repository;
    private AuditProperties properties;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        repository = mock(AuditRepository.class);
        properties = new AuditProperties();
        logger = (Logger) LoggerFactory.getLogger(AuditEventListener.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void savesEvent() {
        AuditEventListener listener = new AuditEventListener(repository, properties, null);

        listener.onAuditEvent(event());

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo("query");
        assertThat(captor.getValue().isSuccess()).isTrue();
    }

    @Test
    void masksWhenMaskTemplateIsPresent() {
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("maskTemplate", new MaskTemplate());
        ObjectProvider<Object> provider = beanFactory.getBeanProvider(Object.class);
        AuditEventListener listener = new AuditEventListener(repository, properties, provider);

        listener.onAuditEvent(event());

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertThat(saved.getOperator()).isEqualTo("138****5678");
        assertThat(saved.getTargetId()).isEqualTo("138****5678");
        assertThat(saved.getArgsSnapshot()).isEqualTo("138****5678");
        assertThat(saved.getResultSummary()).isEqualTo("user:138****5678");
        assertThat(saved.getArgsSnapshot()).doesNotContain("13812345678");
    }

    @Test
    void warnsOnceAndStoresPlaintextWhenMaskTemplateIsMissing() {
        AuditEventListener listener = new AuditEventListener(repository, properties, null);

        listener.onAuditEvent(event());
        listener.onAuditEvent(event());

        assertThat(appender.list)
                .filteredOn(log -> log.getLevel() == Level.WARN)
                .hasSize(1);
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(
                saved -> assertThat(saved.getArgsSnapshot()).isEqualTo("13812345678"));
    }

    @Test
    void doesNotWarnWhenMaskingIsDisabled() {
        properties.setMaskBeforeSave(false);
        AuditEventListener listener = new AuditEventListener(repository, properties, null);

        listener.onAuditEvent(event());

        assertThat(appender.list)
                .filteredOn(log -> log.getLevel() == Level.WARN)
                .isEmpty();
    }

    @Test
    void neverPropagatesRepositoryFailure() {
        doThrow(new IllegalStateException("storage down")).when(repository).save(any(AuditEvent.class));
        AuditEventListener listener = new AuditEventListener(repository, properties, null);

        assertThatCode(() -> listener.onAuditEvent(event())).doesNotThrowAnyException();
    }

    @Test
    void ignoresNullEvent() {
        AuditEventListener listener = new AuditEventListener(repository, properties, null);

        assertThatCode(() -> listener.onAuditEvent(null)).doesNotThrowAnyException();
    }

    private static AuditEvent event() {
        return new AuditEvent(
                "13812345678",
                "query",
                "13812345678",
                "DemoService.query(String)",
                new Object[]{"13812345678"},
                "user:13812345678",
                true,
                null,
                Instant.parse("2026-01-01T00:00:00Z"));
    }
}
