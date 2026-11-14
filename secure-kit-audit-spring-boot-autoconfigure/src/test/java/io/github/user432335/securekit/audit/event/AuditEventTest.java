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
package io.github.user432335.securekit.audit.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventTest {

    @Test
    void snapshotsArgumentsWithoutRetainingTheArray() {
        Object[] args = {"13812345678"};
        AuditEvent event = new AuditEvent(
                "alice", "query", "u-1", "Demo.query(String)", args,
                "result", true, null, Instant.EPOCH);

        args[0] = "changed";

        assertThat(event.getArgsSnapshot()).isEqualTo("13812345678");
        assertThat(event.getResultSummary()).isEqualTo("result");
        assertThat(event.getOccurredAt()).isEqualTo(Instant.EPOCH);
    }

    @Test
    void truncatesSummariesTo512Characters() {
        String longValue = "x".repeat(600);
        AuditEvent event = new AuditEvent(
                "alice", "query", "u-1", "Demo.query(String)",
                new Object[]{longValue}, longValue, false, longValue, Instant.EPOCH);

        assertThat(event.getArgsSnapshot()).hasSize(512);
        assertThat(event.getResultSummary()).hasSize(512);
        assertThat(event.getErrorMessage()).hasSize(512);
    }

    @Test
    void maskedCreatesANewEvent() {
        AuditEvent original = new AuditEvent(
                "alice", "query", "13812345678", "Demo.query(String)",
                new Object[]{"13812345678"}, "user:13812345678", true, null, Instant.EPOCH);

        AuditEvent masked = original.masked(value -> value.replace("13812345678", "138****5678"));

        assertThat(masked).isNotSameAs(original);
        assertThat(masked.getOperator()).isEqualTo("alice");
        assertThat(masked.getTargetId()).isEqualTo("138****5678");
        assertThat(masked.getArgsSnapshot()).isEqualTo("138****5678");
        assertThat(masked.getResultSummary()).isEqualTo("user:138****5678");
        assertThat(original.getArgsSnapshot()).isEqualTo("13812345678");
    }

    @Test
    void toStringContainsReadableFields() {
        AuditEvent event = new AuditEvent(
                "alice", "query", "u-1", "Demo.query(String)",
                new Object[]{"arg"}, "result", true, null, Instant.EPOCH);

        assertThat(event.toString())
                .contains("alice")
                .contains("query")
                .contains("u-1")
                .contains("success=true");
    }
}
