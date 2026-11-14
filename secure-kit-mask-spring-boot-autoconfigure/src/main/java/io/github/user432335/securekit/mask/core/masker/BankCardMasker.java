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
 * Masks bank card numbers.
 *
 * <p>The accepted shape is 13 to 19 digits. The first four and last four digits are kept; the
 * middle segment is replaced.</p>
 *
 * <p>This class is stateless after construction and safe for concurrent use. It never throws for
 * malformed input.</p>
 */
public class BankCardMasker implements Masker.ReplacementAware {

    private static final Pattern DEFAULT_PATTERN = Pattern.compile("^\\d{13,19}$");
    private static final int PREFIX_LENGTH = 4;
    private static final int SUFFIX_LENGTH = 4;

    private final MaskProperties properties;
    private final Pattern validationPattern;

    /**
     * Creates a bank-card masker.
     *
     * @param properties configuration used for the default replacement and an optional validation
     *                   pattern override; may be {@code null}
     */
    public BankCardMasker(MaskProperties properties) {
        this.properties = properties;
        this.validationPattern = resolvePattern(properties);
    }

    /**
     * Returns {@link MaskType#BANK_CARD}.
     *
     * @return the supported category
     */
    @Override
    public MaskType supportedType() {
        return MaskType.BANK_CARD;
    }

    /**
     * Masks a bank card number with the configured default replacement.
     *
     * @param original the original value; may be {@code null}
     * @param pattern  ignored by this built-in masker
     * @return the masked value, or the original value when it is not a valid bank card number
     */
    @Override
    public String mask(String original, String pattern) {
        return mask(original, pattern, null);
    }

    /**
     * Masks a bank card number with an explicit replacement.
     *
     * @param original    the original value; may be {@code null}
     * @param pattern     ignored by this built-in masker
     * @param replacement replacement text for this call; {@code null} or empty falls back to the
     *                    configured default
     * @return the masked value, or the original value when it is not a valid bank card number
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
            String fill = replacementText(replacement);
            return original.substring(0, PREFIX_LENGTH)
                    + fill(fill, original.length() - PREFIX_LENGTH - SUFFIX_LENGTH)
                    + original.substring(original.length() - SUFFIX_LENGTH);
        } catch (RuntimeException ignored) {
            return original;
        }
    }

    private static Pattern resolvePattern(MaskProperties properties) {
        if (properties == null || properties.getPatterns() == null) {
            return DEFAULT_PATTERN;
        }
        String configured = properties.getPatterns().get(MaskType.BANK_CARD);
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
