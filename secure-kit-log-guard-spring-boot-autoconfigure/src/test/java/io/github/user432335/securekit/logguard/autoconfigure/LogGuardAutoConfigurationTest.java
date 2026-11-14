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
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LogGuardAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogGuardAutoConfiguration.class));

    @Test
    void isDisabledByDefault() {
        runner.run(context -> assertThat(context).doesNotHaveBean(SensitivePatternSet.class));
    }

    @Test
    void registersPatternSetWhenEnabled() {
        runner.withPropertyValues("secure-kit.log-guard.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(SensitivePatternSet.class);
            SensitivePatternSet patternSet = context.getBean(SensitivePatternSet.class);
            assertThat(patternSet.size()).isEqualTo(7);
            assertThat(patternSet.getReplacement()).isEqualTo("*");
            assertThat(patternSet.apply("13812345678")).isEqualTo("*");
        });
    }

    @Test
    void bindsReplacementAndPatterns() {
        runner.withPropertyValues(
                        "secure-kit.log-guard.enabled=true",
                        "secure-kit.log-guard.default-replacement=#",
                        "secure-kit.log-guard.patterns[0]=abc")
                .run(context -> {
                    SensitivePatternSet patternSet = context.getBean(SensitivePatternSet.class);
                    assertThat(patternSet.getReplacement()).isEqualTo("#");
                    assertThat(patternSet.size()).isEqualTo(1);
                    assertThat(patternSet.apply("abc")).isEqualTo("#");
                });
    }
}
