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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaskerRegistryTest {

    private final MaskProperties properties = new MaskProperties();
    private final PhoneMasker phoneMasker = new PhoneMasker(properties);
    private final RegexMasker regexMasker = new RegexMasker(properties);

    @Test
    void dispatchesByType() {
        MaskerRegistry registry = new MaskerRegistry(List.of(phoneMasker, regexMasker));
        assertThat(registry.mask(MaskType.PHONE, "13812345678")).isEqualTo("138****5678");
        assertThat(registry.mask(MaskType.CUSTOM, "a1", "\\d", "#")).isEqualTo("a#");
    }

    @Test
    void returnsOriginalForNullType() {
        MaskerRegistry registry = new MaskerRegistry(List.of(phoneMasker));
        assertThat(registry.mask(null, "13812345678")).isEqualTo("13812345678");
        assertThat(registry.supports(null)).isFalse();
    }

    @Test
    void returnsOriginalForUnknownType() {
        MaskerRegistry registry = new MaskerRegistry(List.of(phoneMasker));
        assertThat(registry.mask(MaskType.EMAIL, "a@b.com")).isEqualTo("a@b.com");
        assertThat(registry.supports(MaskType.EMAIL)).isFalse();
    }

    @Test
    void returnsOriginalForNullAndEmptyInput() {
        MaskerRegistry registry = new MaskerRegistry(List.of(phoneMasker));
        assertThat(registry.mask(MaskType.PHONE, null)).isNull();
        assertThat(registry.mask(MaskType.PHONE, "")).isEmpty();
    }

    @Test
    void usesCustomReplacement() {
        MaskerRegistry registry = new MaskerRegistry(List.of(phoneMasker));
        assertThat(registry.mask(MaskType.PHONE, "13812345678", "#"))
                .isEqualTo("138####5678");
    }

    @Test
    void keepsFirstMaskerForDuplicateType() {
        Masker first = fixedMasker(MaskType.PHONE, "first");
        Masker second = fixedMasker(MaskType.PHONE, "second");
        MaskerRegistry registry = new MaskerRegistry(List.of(first, second));
        assertThat(registry.mask(MaskType.PHONE, "ignored")).isEqualTo("first");
    }

    @Test
    void ignoresNullElementsAndNullSupportedTypes() {
        Masker nullType = fixedMasker(null, "never");
        MaskerRegistry registry = new MaskerRegistry(Arrays.asList(null, nullType, phoneMasker));
        assertThat(registry.supports(MaskType.PHONE)).isTrue();
        assertThat(registry.mask(MaskType.PHONE, "13812345678")).isEqualTo("138****5678");
    }

    @Test
    void emptyOrNullListProducesEmptyRegistry() {
        MaskerRegistry nullList = new MaskerRegistry(null);
        MaskerRegistry emptyList = new MaskerRegistry(List.of());
        assertThat(nullList.supports(MaskType.PHONE)).isFalse();
        assertThat(nullList.mask(MaskType.PHONE, "13812345678")).isEqualTo("13812345678");
        assertThat(emptyList.supports(MaskType.PHONE)).isFalse();
        assertThat(emptyList.mask(MaskType.PHONE, "13812345678")).isEqualTo("13812345678");
    }

    @Test
    void takesDefensiveCopy() {
        List<Masker> mutable = new ArrayList<>();
        mutable.add(phoneMasker);
        MaskerRegistry registry = new MaskerRegistry(mutable);
        mutable.clear();
        assertThat(registry.supports(MaskType.PHONE)).isTrue();
    }

    @Test
    void returnsOriginalWhenMaskerThrows() {
        Masker throwing = new Masker() {
            @Override
            public MaskType supportedType() {
                return MaskType.PHONE;
            }

            @Override
            public String mask(String original, String pattern) {
                throw new IllegalStateException("boom");
            }
        };
        MaskerRegistry registry = new MaskerRegistry(List.of(throwing));
        assertThat(registry.mask(MaskType.PHONE, "13812345678")).isEqualTo("13812345678");
    }

    private static Masker fixedMasker(MaskType type, String result) {
        return new Masker() {
            @Override
            public MaskType supportedType() {
                return type;
            }

            @Override
            public String mask(String original, String pattern) {
                return result;
            }
        };
    }
}
