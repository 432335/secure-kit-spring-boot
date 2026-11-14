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

class IdCardMaskerTest {

    private final IdCardMasker masker = new IdCardMasker(new MaskProperties());

    @Test
    void masksNormalIdentityCard() {
        assertThat(masker.mask("110101199001011234", null))
                .isEqualTo("110101********1234");
    }

    @Test
    void acceptsUppercaseAndLowercaseX() {
        assertThat(masker.mask("11010119900101123X", null))
                .isEqualTo("110101********123X");
        assertThat(masker.mask("11010119900101123x", null))
                .isEqualTo("110101********123x");
    }

    @Test
    void returnsNullAndEmptyUnchanged() {
        assertThat(masker.mask(null, null)).isNull();
        assertThat(masker.mask("", null)).isEmpty();
    }

    @Test
    void returnsOriginalForWrongLength() {
        assertThat(masker.mask("11010119900101123", null))
                .isEqualTo("11010119900101123");
        assertThat(masker.mask("1101011990010112345", null))
                .isEqualTo("1101011990010112345");
    }

    @Test
    void returnsOriginalForIllegalCharacters() {
        assertThat(masker.mask("11010119900101123A", null))
                .isEqualTo("11010119900101123A");
        assertThat(masker.mask("11010119900101123 ", null))
                .isEqualTo("11010119900101123 ");
    }

    @Test
    void usesExplicitReplacement() {
        assertThat(masker.mask("110101199001011234", null, "#"))
                .isEqualTo("110101########1234");
    }

    @Test
    void usesConfiguredReplacement() {
        MaskProperties properties = new MaskProperties();
        properties.setDefaultReplacement("#");
        assertThat(new IdCardMasker(properties).mask("110101199001011234", null))
                .isEqualTo("110101########1234");
    }
}
