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

import io.github.user432335.securekit.core.MaskType;
import io.github.user432335.securekit.mask.properties.MaskProperties;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneMaskerTest {

    private final PhoneMasker masker = new PhoneMasker(new MaskProperties());

    @Test
    void masksNormalPhoneNumber() {
        assertThat(masker.mask("13812345678", null)).isEqualTo("138****5678");
    }

    @Test
    void keepsBoundaryPrefixesAndSuffixes() {
        assertThat(masker.mask("13000000000", null)).isEqualTo("130****0000");
        assertThat(masker.mask("19999999999", null)).isEqualTo("199****9999");
    }

    @Test
    void returnsNullForNull() {
        assertThat(masker.mask(null, null)).isNull();
    }

    @Test
    void returnsEmptyForEmpty() {
        assertThat(masker.mask("", null)).isEmpty();
    }

    @Test
    void returnsOriginalForWrongLength() {
        assertThat(masker.mask("1381234567", null)).isEqualTo("1381234567");
        assertThat(masker.mask("138123456789", null)).isEqualTo("138123456789");
    }

    @Test
    void returnsOriginalForIllegalCharactersOrPrefix() {
        assertThat(masker.mask("23812345678", null)).isEqualTo("23812345678");
        assertThat(masker.mask("1381234567a", null)).isEqualTo("1381234567a");
    }

    @Test
    void usesExplicitReplacement() {
        assertThat(masker.mask("13812345678", null, "#")).isEqualTo("138####5678");
        assertThat(masker.mask("13812345678", null, "ab")).isEqualTo("138abab5678");
    }

    @Test
    void usesConfiguredReplacement() {
        MaskProperties properties = new MaskProperties();
        properties.setDefaultReplacement("#");
        assertThat(new PhoneMasker(properties).mask("13812345678", null))
                .isEqualTo("138####5678");
    }

    @Test
    void fallsBackToStarForBlankReplacement() {
        MaskProperties properties = new MaskProperties();
        properties.setDefaultReplacement("");
        assertThat(new PhoneMasker(properties).mask("13812345678", null, ""))
                .isEqualTo("138****5678");
    }

    @Test
    void honoursValidationPatternOverride() {
        MaskProperties properties = new MaskProperties();
        EnumMap<MaskType, String> patterns = new EnumMap<>(MaskType.class);
        patterns.put(MaskType.PHONE, "^1[0-9]{10}$");
        properties.setPatterns(patterns);
        PhoneMasker custom = new PhoneMasker(properties);
        assertThat(custom.mask("12812345678", null)).isEqualTo("128****5678");
    }

    @Test
    void ignoresInvalidValidationPatternOverride() {
        MaskProperties properties = new MaskProperties();
        EnumMap<MaskType, String> patterns = new EnumMap<>(MaskType.class);
        patterns.put(MaskType.PHONE, "[");
        properties.setPatterns(patterns);
        PhoneMasker custom = new PhoneMasker(properties);
        assertThat(custom.mask("13812345678", null)).isEqualTo("138****5678");
    }
}
