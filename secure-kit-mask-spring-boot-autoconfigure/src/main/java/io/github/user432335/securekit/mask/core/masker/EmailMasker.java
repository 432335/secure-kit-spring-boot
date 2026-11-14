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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Masks email addresses.
 *
 * <p>The accepted shape is {@code ^([^@\s]+)@([^@\s]+)$}. The first character of the local part is
 * kept, the remaining local characters are replaced, and the domain is kept intact. A local part
 * with exactly one character is returned unchanged because there is nothing to mask.</p>
 *
 * <p>This class is stateless after construction and safe for concurrent use. It never throws for
 * malformed input.</p>
 */
public class EmailMasker implements Masker.ReplacementAware {

    private static final Pattern DEFAULT_PATTERN = Pattern.compile("^([^@\\s]+)@([^@\\s]+)$");

    private final MaskProperties properties;
    private final Pattern validationPattern;

    /**
     * Creates an email masker.
     *
     * @param properties configuration used for the default replacement and an optional validation
     *                   pattern override; may be {@code null}
     */
    public EmailMasker(MaskProperties properties) {
        this.properties = properties;
        this.validationPattern = resolvePattern(properties);
    }

    /**
     * Returns {@link MaskType#EMAIL}.
     *
     * @return the supported category
     */
    @Override
    public MaskType supportedType() {
        return MaskType.EMAIL;
    }

    /**
     * Masks an email address with the configured default replacement.
     *
     * @param original the original value; may be {@code null}
     * @param pattern  ignored by this built-in masker
     * @return the masked value, or the original value when it is not a valid email address
     */
    @Override
    public String mask(String original, String pattern) {
        return mask(original, pattern, null);
    }

    /**
     * Masks an email address with an explicit replacement.
     *
     * @param original    the original value; may be {@code null}
     * @param pattern     ignored by this built-in masker
     * @param replacement replacement text for this call; {@code null} or empty falls back to the
     *                    configured default
     * @return the masked value, or the original value when it is not a valid email address
     */
    @Override
    public String mask(String original, String pattern, String replacement) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        try {
            if (!validationPattern.matcher(original).matches()) {
                return original;
            }
            int at = original.indexOf('@');
            if (at <= 0 || at == original.length() - 1) {
                return original;
            }
            String local = original.substring(0, at);
            if (local.length() <= 1) {
                return original;
            }
            String domain = original.substring(at + 1);
            String fill = replacementText(replacement);
            return local.charAt(0) + fill(fill, local.length() - 1) + "@" + domain;
        } catch (RuntimeException ignored) {
            return original;
        }
    }

    private static Pattern resolvePattern(MaskProperties properties) {
        if (properties == null || properties.getPatterns() == null) {
            return DEFAULT_PATTERN;
        }
        String configured = properties.getPatterns().get(MaskType.EMAIL);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_PATTERN;
        }
        try {
            return Pattern.compile(configured);
        } catch (PatternSyntaxException ignored) {
            return DEFAULT_PATTERN;
        }
    }

    private String replacementText(String override) {
        if (override != null && !override.isEmpty()) {
            return override;
        }
        String configured = properties == null ? null : properties.getDefaultReplacement();
        return configured == null || configured.isEmpty() ? "*" : configured;
    }

    private static String fill(String replacement, int length) {
        if (length <= 0) {
            return "";
        }
        int repeats = (length + replacement.length() - 1) / replacement.length();
        return replacement.repeat(repeats).substring(0, length);
    }
}
