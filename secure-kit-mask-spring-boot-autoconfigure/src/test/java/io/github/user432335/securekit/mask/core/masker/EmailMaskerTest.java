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

class EmailMaskerTest {

    private final EmailMasker masker = new EmailMasker(new MaskProperties());

    @Test
    void masksLocalPartAndKeepsDomain() {
        assertThat(masker.mask("zhangsan@example.com", null))
                .isEqualTo("z*******@example.com");
    }

    @Test
    void returnsSingleCharacterLocalPartUnchanged() {
        assertThat(masker.mask("a@example.com", null)).isEqualTo("a@example.com");
    }

    @Test
    void returnsNullAndEmptyUnchanged() {
        assertThat(masker.mask(null, null)).isNull();
        assertThat(masker.mask("", null)).isEmpty();
    }

    @Test
    void returnsOriginalForMalformedAddress() {
        assertThat(masker.mask("zhangsan.example.com", null))
                .isEqualTo("zhangsan.example.com");
        assertThat(masker.mask("zhang san@example.com", null))
                .isEqualTo("zhang san@example.com");
        assertThat(masker.mask("a@b@example.com", null))
                .isEqualTo("a@b@example.com");
    }

    @Test
    void usesExplicitReplacement() {
        assertThat(masker.mask("zhangsan@example.com", null, "#"))
                .isEqualTo("z#######@example.com");
    }

    @Test
    void usesConfiguredReplacement() {
        MaskProperties properties = new MaskProperties();
        properties.setDefaultReplacement("#");
        assertThat(new EmailMasker(properties).mask("zhangsan@example.com", null))
                .isEqualTo("z#######@example.com");
    }
}
