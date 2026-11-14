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
package io.github.user432335.securekit.audit.aop;

import io.github.user432335.securekit.audit.annotation.AuditLog;
import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.properties.AuditProperties;
import io.github.user432335.securekit.audit.spi.AuditUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditAspectTest {

    private AuditUserProvider userProvider;
    private ApplicationEventPublisher publisher;
    private Target target;

    @BeforeEach
    void setUp() {
        userProvider = mock(AuditUserProvider.class);
        publisher = mock(ApplicationEventPublisher.class);
        when(userProvider.currentOperator()).thenReturn("alice");

        AuditAspect aspect = new AuditAspect(userProvider, publisher, new AuditProperties());
        AspectJProxyFactory factory = new AspectJProxyFactory(new Target());
        factory.addAspect(aspect);
        target = factory.getProxy();
    }

    @Test
    void publishesSuccessfulEventWithEvaluatedTargetId() {
        String result = target.find(new User("u-1"));

        assertThat(result).isEqualTo("found");
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(publisher).publishEvent(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.getOperator()).isEqualTo("alice");
        assertThat(event.getAction()).isEqualTo("find");
        assertThat(event.getTargetId()).isEqualTo("u-1");
        assertThat(event.isSuccess()).isTrue();
        assertThat(event.getArgsSnapshot()).contains("u-1");
        assertThat(event.getResultSummary()).isEqualTo("found");
    }

    @Test
    void usesResultExpression() {
        String result = target.resultId(new User("u-2"));

        assertThat(result).isEqualTo("u-2");
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTargetId()).isEqualTo("u-2");
    }

    @Test
    void ignoresSpelFailureAndKeepsBusinessResult() {
        assertThatCode(() -> target.badExpression("x")).doesNotThrowAnyException();

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTargetId()).isNull();
        assertThat(captor.getValue().isSuccess()).isTrue();
    }

    @Test
    void publishesFailureEventAndRethrowsOriginalException() {
        assertThatThrownBy(() -> target.fail("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(publisher).publishEvent(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.isSuccess()).isFalse();
        assertThat(event.getErrorMessage()).isEqualTo("boom");
        assertThat(event.getTargetId()).isEqualTo("x");
    }

    @Test
    void ignoresPublisherFailure() {
        doThrow(new IllegalStateException("publisher failed"))
                .when(publisher).publishEvent(any(Object.class));

        assertThat(target.find(new User("u-3"))).isEqualTo("found");
    }

    @Test
    void fallsBackWhenUserProviderFails() {
        when(userProvider.currentOperator()).thenThrow(new IllegalStateException("no principal"));

        target.find(new User("u-4"));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getOperator()).isEqualTo("anonymous");
    }

    record User(String userId) {
    }

    static class Target {

        @AuditLog(action = "find", targetIdExpression = "#p0.userId")
        public String find(User user) {
            return "found";
        }

        @AuditLog(action = "result-id", targetIdExpression = "#result")
        public String resultId(User user) {
            return user.userId();
        }

        @AuditLog(action = "bad-expression", targetIdExpression = "#p0.missing")
        public String badExpression(String value) {
            return value;
        }

        @AuditLog(action = "fail", targetIdExpression = "#p0")
        public String fail(String value) {
            throw new IllegalStateException("boom");
        }
    }
}
