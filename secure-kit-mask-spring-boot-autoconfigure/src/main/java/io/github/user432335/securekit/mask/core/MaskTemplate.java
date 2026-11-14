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

import java.util.Objects;

/**
 * Facade for explicit masking calls.
 *
 * <p>Business code should depend on this facade rather than on {@link MaskerRegistry} directly.
 * The facade delegates every call to the registry and adds no automatic interception. This is the
 * primary usage style of the mask capability library: the caller decides when masking happens.</p>
 *
 * <p>This class is immutable and safe for concurrent use when the underlying registry is safe for
 * concurrent use.</p>
 */
public final class MaskTemplate {

    private final MaskerRegistry registry;

    /**
     * Creates a template backed by the supplied registry.
     *
     * @param registry the registry to delegate to; must not be {@code null}
     * @throws NullPointerException when {@code registry} is {@code null}
     */
    public MaskTemplate(MaskerRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    /**
     * Masks a value with the registered masker's default replacement.
     *
     * @param type     the category; may be {@code null}
     * @param original the original value; may be {@code null}
     * @return the masked value, or the original value when no masker is available
     */
    public String mask(MaskType type, String original) {
        return registry.mask(type, original);
    }

    /**
     * Masks a value with an explicit replacement.
     *
     * @param type        the category; may be {@code null}
     * @param original    the original value; may be {@code null}
     * @param replacement replacement text for this call; may be {@code null}
     * @return the masked value, or the original value when no masker is available
     */
    public String mask(MaskType type, String original, String replacement) {
        return registry.mask(type, original, replacement);
    }

    /**
     * Masks a value with a caller-supplied pattern and replacement.
     *
     * @param type        the category; may be {@code null}
     * @param original    the original value; may be {@code null}
     * @param pattern     a caller-supplied regular expression, used by the {@code CUSTOM} masker
     * @param replacement replacement text for this call; may be {@code null}
     * @return the masked value, or the original value when no masker is available
     */
    public String mask(MaskType type, String original, String pattern, String replacement) {
        return registry.mask(type, original, pattern, replacement);
    }
}
