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

class BankCardMaskerTest {

    private final BankCardMasker masker = new BankCardMasker(new MaskProperties());

    @Test
    void masksThirteenDigitCard() {
        assertThat(masker.mask("6222021234567", null)).isEqualTo("6222*****4567");
    }

    @Test
    void masksNineteenDigitCard() {
        assertThat(masker.mask("6222021234567890123", null))
                .isEqualTo("6222***********0123");
    }

    @Test
    void returnsNullAndEmptyUnchanged() {
        assertThat(masker.mask(null, null)).isNull();
        assertThat(masker.mask("", null)).isEmpty();
    }

    @Test
    void returnsOriginalForWrongLength() {
        assertThat(masker.mask("622202123456", null)).isEqualTo("622202123456");
        assertThat(masker.mask("62220212345678901234", null))
                .isEqualTo("62220212345678901234");
    }

    @Test
    void returnsOriginalForIllegalCharacters() {
        assertThat(masker.mask("622202123456a", null)).isEqualTo("622202123456a");
        assertThat(masker.mask("6222 0212 3456", null)).isEqualTo("6222 0212 3456");
    }

    @Test
    void usesExplicitReplacement() {
        assertThat(masker.mask("6222021234567", null, "#")).isEqualTo("6222#####4567");
    }
}
