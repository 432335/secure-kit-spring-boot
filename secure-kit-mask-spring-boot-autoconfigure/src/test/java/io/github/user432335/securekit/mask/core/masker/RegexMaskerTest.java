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
package io.github.user432335.securekit.mask.core.masker;

import io.github.user432335.securekit.mask.properties.MaskProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexMaskerTest {

    private final RegexMasker masker = new RegexMasker(new MaskProperties());

    @Test
    void replacesMatches() {
        assertThat(masker.mask("order-1234", "\\d{4}")).isEqualTo("order-*");
    }

    @Test
    void usesExplicitReplacement() {
        assertThat(masker.mask("order-1234", "\\d{4}", "#")).isEqualTo("order-#");
    }

    @Test
    void returnsNullAndEmptyUnchanged() {
        assertThat(masker.mask(null, "\\d")).isNull();
        assertThat(masker.mask("", "\\d")).isEmpty();
    }

    @Test
    void returnsOriginalForBlankPattern() {
        assertThat(masker.mask("order-1234", "")).isEqualTo("order-1234");
        assertThat(masker.mask("order-1234", "   ")).isEqualTo("order-1234");
        assertThat(masker.mask("order-1234", null)).isEqualTo("order-1234");
    }

    @Test
    void returnsOriginalForInvalidPattern() {
        assertThat(masker.mask("order-1234", "[")).isEqualTo("order-1234");
    }

    @Test
    void returnsOriginalWhenNoMatch() {
        assertThat(masker.mask("order-1234", "[A-Z]+")).isEqualTo("order-1234");
    }

    @Test
    void quotesReplacementDollarSigns() {
        assertThat(masker.mask("abc", "b", "$1")).isEqualTo("a$1c");
    }

    @Test
    void usesConfiguredReplacement() {
        MaskProperties properties = new MaskProperties();
        properties.setDefaultReplacement("#");
        assertThat(new RegexMasker(properties).mask("abc", "b")).isEqualTo("a#c");
    }
}
