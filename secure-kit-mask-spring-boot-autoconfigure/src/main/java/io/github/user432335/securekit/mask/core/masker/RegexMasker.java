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
import io.github.user432335.securekit.mask.core.Masker;
import io.github.user432335.securekit.mask.properties.MaskProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Masks values using a caller-supplied regular expression.
 *
 * <p>Only {@link MaskType#CUSTOM} is supported. Every match is replaced with the configured
 * replacement text. An empty or invalid pattern returns the original value. The regular
 * expression is supplied per call, so it cannot be precompiled at class initialization time; the
 * class compiles it for each invocation and never caches caller input.</p>
 *
 * <p>This class is stateless after construction and safe for concurrent use. It never throws for
 * malformed input.</p>
 */
public class RegexMasker implements Masker.ReplacementAware {

    private final MaskProperties properties;

    /**
     * Creates a custom regular-expression masker.
     *
     * @param properties configuration used for the default replacement; may be {@code null}
     */
    public RegexMasker(MaskProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns {@link MaskType#CUSTOM}.
     *
     * @return the supported category
     */
    @Override
    public MaskType supportedType() {
        return MaskType.CUSTOM;
    }

    /**
     * Masks all matches with the configured default replacement.
     *
     * @param original the original value; may be {@code null}
     * @param pattern  the regular expression; empty or invalid values return the original
     * @return the masked value, or the original value when masking is not possible
     */
    @Override
    public String mask(String original, String pattern) {
        return mask(original, pattern, null);
    }

    /**
     * Masks all matches with an explicit replacement.
     *
     * @param original    the original value; may be {@code null}
     * @param pattern     the regular expression; empty or invalid values return the original
     * @param replacement replacement text for this call; {@code null} or empty falls back to the
     *                    configured default
     * @return the masked value, or the original value when masking is not possible
     */
    @Override
    public String mask(String original, String pattern, String replacement) {
        if (original == null || original.isEmpty() || pattern == null || pattern.isBlank()) {
            return original;
        }
        try {
            Pattern compiled = Pattern.compile(pattern);
            return compiled.matcher(original)
                    .replaceAll(Matcher.quoteReplacement(replacementText(replacement)));
        } catch (PatternSyntaxException ignored) {
            return original;
        } catch (RuntimeException ignored) {
            return original;
        }
    }

    private String replacementText(String override) {
        if (override != null && !override.isEmpty()) {
            return override;
        }
        String configured = properties == null ? null : properties.getDefaultReplacement();
        return configured == null || configured.isEmpty() ? "*" : configured;
    }
}
