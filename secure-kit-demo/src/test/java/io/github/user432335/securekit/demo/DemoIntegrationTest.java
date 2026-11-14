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
package io.github.user432335.securekit.demo;

import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import io.github.user432335.securekit.audit.spi.AuditUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "secure-kit.mask.enabled=true",
                "secure-kit.audit.enabled=true",
                "secure-kit.audit.async=false"
        })
@Import(DemoIntegrationTest.TestConfig.class)
class DemoIntegrationTest {

    @Autowired
    private DemoService demoService;

    @Autowired
    private CapturingAuditRepository auditRepository;

    @Test
    void auditEventContainsMaskedValuesAndOperator() {
        String result = demoService.queryUser("13812345678");

        assertThat(result).isEqualTo("user:13812345678");
        assertThat(auditRepository.events).hasSize(1);
        AuditEvent event = auditRepository.events.get(0);
        assertThat(event.getOperator()).isEqualTo("alice");
        assertThat(event.isSuccess()).isTrue();
        assertThat(event.getAction()).isEqualTo("query-user");
        assertThat(event.getTargetId()).isEqualTo("138****5678");
        assertThat(event.getArgsSnapshot()).isEqualTo("138****5678");
        assertThat(event.getResultSummary()).doesNotContain("13812345678");
        assertThat(event.getResultSummary()).contains("*");
        assertThat(event.getArgsSnapshot()).doesNotContain("13812345678");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        CapturingAuditRepository capturingAuditRepository() {
            return new CapturingAuditRepository();
        }

        @Bean
        AuditUserProvider auditUserProvider() {
            return () -> "alice";
        }
    }

    static class CapturingAuditRepository implements AuditRepository {

        private final List<AuditEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void save(AuditEvent event) {
            events.add(event);
        }
    }
}
