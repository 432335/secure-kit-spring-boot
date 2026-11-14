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

/**
 * Strategy that masks one category of value.
 *
 * <p>Implementations must be stateless after construction and safe for concurrent use. The mask
 * capability library is a library: it never schedules work and never mutates caller objects.</p>
 *
 * <p>Every implementation must honour the following contract:</p>
 * <ul>
 *     <li>{@code original == null} or an empty string is returned unchanged ({@code null} stays
 *     {@code null}).</li>
 *     <li>A value that does not match the category rules is returned unchanged.</li>
 *     <li>No exception is thrown for malformed input. In particular, an invalid
 *     {@link MaskType#CUSTOM} regular expression is caught and the original value is returned.</li>
 *     <li>The replacement text is taken from {@code MaskProperties.defaultReplacement}; a
 *     {@code null} or empty value falls back to {@code "*"}.</li>
 *     <li>The masked middle segment is filled by repeating the replacement text and truncating it
 *     to the required length.</li>
 * </ul>
 */
public interface Masker {

    /**
     * Returns the category handled by this masker.
     *
     * @return the supported category, never {@code null}
     */
    MaskType supportedType();

    /**
     * Masks a value.
     *
     * @param original the original value; may be {@code null}
     * @param pattern  a caller-supplied regular expression, used by the {@link MaskType#CUSTOM}
     *                 masker; built-in maskers ignore it and use their configured validation
     *                 pattern
     * @return the masked value, or the original value when masking is not possible; never throws
     *         for malformed input
     */
    String mask(String original, String pattern);

    /**
     * Optional extension implemented by maskers that can apply a caller-supplied replacement for a
     * single call.
     *
     * <p>Implementations must be thread-safe. A custom masker does not need to implement this
     * interface; when it does not, {@link MaskerRegistry} falls back to the two-argument
     * {@link #mask(String, String)} method and the configured default replacement.</p>
     */
    interface ReplacementAware extends Masker {

        /**
         * Masks a value with an explicit replacement.
         *
         * @param original    the original value; may be {@code null}
         * @param pattern     a caller-supplied regular expression, used by
         *                    {@link MaskType#CUSTOM}
         * @param replacement replacement text for this call; {@code null} or empty falls back to
         *                    the configured default
         * @return the masked value, or the original value when masking is not possible; never
         *         throws for malformed input
         */
        String mask(String original, String pattern, String replacement);
    }
}
