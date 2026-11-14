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
package io.github.user432335.securekit.mask.autoconfigure;

import io.github.user432335.securekit.core.MaskType;
import io.github.user432335.securekit.mask.core.MaskTemplate;
import io.github.user432335.securekit.mask.core.Masker;
import io.github.user432335.securekit.mask.core.MaskerRegistry;
import io.github.user432335.securekit.mask.core.masker.PhoneMasker;
import io.github.user432335.securekit.mask.properties.MaskProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MaskAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MaskAutoConfiguration.class));

    @Test
    void isDisabledByDefault() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(MaskTemplate.class);
            assertThat(context).doesNotHaveBean(MaskerRegistry.class);
            assertThat(context).doesNotHaveBean(PhoneMasker.class);
        });
    }

    @Test
    void registersBeansWhenEnabled() {
        runner.withPropertyValues("secure-kit.mask.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(MaskTemplate.class);
            assertThat(context).hasSingleBean(MaskerRegistry.class);
            assertThat(context).hasSingleBean(PhoneMasker.class);
            assertThat(context).hasSingleBean(io.github.user432335.securekit.mask.core.masker.IdCardMasker.class);
            assertThat(context).hasSingleBean(io.github.user432335.securekit.mask.core.masker.BankCardMasker.class);
            assertThat(context).hasSingleBean(io.github.user432335.securekit.mask.core.masker.EmailMasker.class);
            assertThat(context).hasSingleBean(io.github.user432335.securekit.mask.core.masker.RegexMasker.class);
            assertThat(context.getBean(MaskTemplate.class).mask(MaskType.PHONE, "13812345678"))
                    .isEqualTo("138****5678");
        });
    }

    @Test
    void backsOffForUserDefinedMasker() {
        runner.withPropertyValues("secure-kit.mask.enabled=true")
                .withUserConfiguration(CustomMaskerConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(PhoneMasker.class);
                    assertThat(context.getBean(MaskTemplate.class).mask(MaskType.PHONE, "ignored"))
                            .isEqualTo("custom");
                });
    }

    @Test
    void bindsDefaultReplacement() {
        runner.withPropertyValues(
                        "secure-kit.mask.enabled=true",
                        "secure-kit.mask.default-replacement=#")
                .run(context -> assertThat(context.getBean(MaskTemplate.class)
                        .mask(MaskType.PHONE, "13812345678"))
                        .isEqualTo("138####5678"));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomMaskerConfiguration {

        @Bean
        PhoneMasker phoneMasker(MaskProperties properties) {
            return new PhoneMasker(properties) {
                @Override
                public String mask(String original, String pattern) {
                    return "custom";
                }

                @Override
                public String mask(String original, String pattern, String replacement) {
                    return "custom";
                }
            };
        }
    }
}
