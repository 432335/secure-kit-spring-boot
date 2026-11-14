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

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable lookup table of maskers keyed by category.
 *
 * <p>The registry takes a defensive copy of the supplied list, ignores {@code null} elements and
 * keeps the first masker for each duplicated category. The resulting map is unmodifiable and the
 * registry is safe for concurrent use when the supplied maskers are thread-safe.</p>
 *
 * <p>Every public mask method returns the original value when the category is {@code null}, when
 * no masker is registered for the category, or when a masker fails. No method throws because of
 * malformed input.</p>
 */
public final class MaskerRegistry {

    private final Map<MaskType, Masker> maskers;

    /**
     * Creates a registry from the supplied maskers.
     *
     * <p>{@code null} or an empty list produces an empty registry. Duplicate categories keep the
     * first non-null masker. A masker whose {@link Masker#supportedType()} returns {@code null} is
     * ignored.</p>
     *
     * @param maskers the maskers to register; may be {@code null}
     */
    public MaskerRegistry(List<Masker> maskers) {
        EnumMap<MaskType, Masker> copy = new EnumMap<>(MaskType.class);
        if (maskers != null) {
            for (Masker masker : maskers) {
                if (masker == null) {
                    continue;
                }
                MaskType type = masker.supportedType();
                if (type != null) {
                    copy.putIfAbsent(type, masker);
                }
            }
        }
        this.maskers = Collections.unmodifiableMap(copy);
    }

    /**
     * Masks a value with the registered masker's default replacement.
     *
     * @param type     the category; may be {@code null}
     * @param original the original value; may be {@code null}
     * @return the masked value, or the original value when no masker is available
     */
    public String mask(MaskType type, String original) {
        return mask(type, original, null, null);
    }

    /**
     * Masks a value with an explicit replacement.
     *
     * <p>If the registered masker does not implement {@link Masker.ReplacementAware}, the
     * replacement is ignored and the masker's configured default is used.</p>
     *
     * @param type        the category; may be {@code null}
     * @param original    the original value; may be {@code null}
     * @param replacement replacement text for this call; may be {@code null}
     * @return the masked value, or the original value when no masker is available
     */
    public String mask(MaskType type, String original, String replacement) {
        return mask(type, original, null, replacement);
    }

    /**
     * Masks a value with a caller-supplied pattern and replacement.
     *
     * @param type        the category; may be {@code null}
     * @param original    the original value; may be {@code null}
     * @param pattern     a caller-supplied regular expression, used by the {@code CUSTOM} masker
     * @param replacement replacement text for this call; may be {@code null}
     * @return the masked value, or the original value when no masker is available or masking fails
     */
    public String mask(MaskType type, String original, String pattern, String replacement) {
        if (original == null || original.isEmpty() || type == null) {
            return original;
        }
        Masker masker = maskers.get(type);
        if (masker == null) {
            return original;
        }
        try {
            if (masker instanceof Masker.ReplacementAware replacementAware) {
                return replacementAware.mask(original, pattern, replacement);
            }
            return masker.mask(original, pattern);
        } catch (RuntimeException ignored) {
            return original;
        }
    }

    /**
     * Returns whether a masker is registered for the category.
     *
     * @param type the category; may be {@code null}
     * @return {@code true} only when a masker is registered for a non-null category
     */
    public boolean supports(MaskType type) {
        return type != null && maskers.containsKey(type);
    }
}
