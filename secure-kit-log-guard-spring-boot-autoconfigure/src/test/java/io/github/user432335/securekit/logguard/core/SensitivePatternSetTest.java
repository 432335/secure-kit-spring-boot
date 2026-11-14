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
package io.github.user432335.securekit.logguard.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensitivePatternSetTest {

    private final SensitivePatternSet patternSet = new SensitivePatternSet();

    @Test
    void masksPhoneNumber() {
        assertThat(patternSet.apply("phone 13812345678")).isEqualTo("phone *");
    }

    @Test
    void masksIdentityCard() {
        assertThat(patternSet.apply("id 110101199001011234")).isEqualTo("id *");
    }

    @Test
    void masksAuthorizationHeader() {
        assertThat(patternSet.apply("Authorization: abc123")).isEqualTo("*");
    }

    @Test
    void masksPasswordAssignment() {
        assertThat(patternSet.apply("password=secret")).isEqualTo("*");
    }

    @Test
    void masksBearerToken() {
        assertThat(patternSet.apply("bearer abc")).isEqualTo("*");
    }

    @Test
    void masksEmail() {
        assertThat(patternSet.apply("mail user@example.com")).isEqualTo("mail *");
    }

    @Test
    void returnsNullForNull() {
        assertThat(patternSet.apply(null)).isNull();
    }

    @Test
    void returnsEmptyForEmpty() {
        assertThat(patternSet.apply("")).isEmpty();
    }

    @Test
    void usesCustomReplacement() {
        SensitivePatternSet custom = new SensitivePatternSet(List.of("1[3-9]\\d{9}"), "#");
        assertThat(custom.apply("13812345678")).isEqualTo("#");
    }

    @Test
    void skipsInvalidPattern() {
        SensitivePatternSet custom = new SensitivePatternSet(List.of("[", "\\d"), "*");
        assertThat(custom.size()).isEqualTo(1);
        assertThat(custom.getSkippedPatterns()).containsExactly("[");
        assertThat(custom.apply("a1")).isEqualTo("a*");
    }

    @Test
    void emptyPatternListLeavesInputUnchanged() {
        SensitivePatternSet custom = new SensitivePatternSet(List.of(), "*");
        assertThat(custom.size()).isZero();
        assertThat(custom.apply("13812345678")).isEqualTo("13812345678");
    }

    @Test
    void noArgConstructorReadsSystemProperties() {
        String oldReplacement = System.getProperty("secure-kit.log-guard.default-replacement");
        String oldPatterns = System.getProperty("secure-kit.log-guard.patterns");
        try {
            System.setProperty("secure-kit.log-guard.default-replacement", "#");
            System.setProperty("secure-kit.log-guard.patterns", "1[3-9]\\d{9}");
            SensitivePatternSet custom = new SensitivePatternSet();
            assertThat(custom.getReplacement()).isEqualTo("#");
            assertThat(custom.size()).isEqualTo(1);
            assertThat(custom.apply("13812345678")).isEqualTo("#");
        } finally {
            restore("secure-kit.log-guard.default-replacement", oldReplacement);
            restore("secure-kit.log-guard.patterns", oldPatterns);
        }
    }

    private static void restore(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
