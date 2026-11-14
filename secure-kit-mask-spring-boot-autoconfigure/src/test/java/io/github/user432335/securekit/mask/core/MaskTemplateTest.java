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
package io.github.user432335.securekit.mask.core;

import io.github.user432335.securekit.core.MaskType;
import io.github.user432335.securekit.mask.core.masker.PhoneMasker;
import io.github.user432335.securekit.mask.core.masker.RegexMasker;
import io.github.user432335.securekit.mask.properties.MaskProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaskTemplateTest {

    private final MaskTemplate template = new MaskTemplate(new MaskerRegistry(List.of(
            new PhoneMasker(new MaskProperties()),
            new RegexMasker(new MaskProperties()))));

    @Test
    void masksWithDefaultReplacement() {
        assertThat(template.mask(MaskType.PHONE, "13812345678")).isEqualTo("138****5678");
    }

    @Test
    void masksWithExplicitReplacement() {
        assertThat(template.mask(MaskType.PHONE, "13812345678", "#"))
                .isEqualTo("138####5678");
    }

    @Test
    void masksWithPatternAndReplacement() {
        assertThat(template.mask(MaskType.CUSTOM, "order-1234", "\\d{4}", "#"))
                .isEqualTo("order-#");
    }
}
