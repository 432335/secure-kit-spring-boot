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
package io.github.user432335.securekit.logguard.core;

import io.github.user432335.securekit.logguard.properties.LogGuardProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Immutable set of precompiled sensitive patterns and a replacement text.
 *
 * <p>The class is used by both logging-framework extension points. Logback and Log4j2 instantiate
 * their converters/policies themselves, so Spring cannot inject this bean into them. To support
 * that case the no-argument constructor reads these optional system properties:</p>
 * <ul>
 *     <li>{@code secure-kit.log-guard.default-replacement}</li>
 *     <li>{@code secure-kit.log-guard.patterns} (comma-separated)</li>
 * </ul>
 * <p>The properties constructor is used by the Spring auto-configuration. Both construction paths
 * produce the same behaviour. Invalid expressions are skipped and exposed through
 * {@link #getSkippedPatterns()}; this class does not log, to avoid recursion through the logging
 * framework.</p>
 *
 * <p>Instances are immutable and safe for concurrent use.</p>
 */
public final class SensitivePatternSet {

    /**
     * Default sensitive expressions, in application order.
     */
    public static final List<String> DEFAULT_PATTERNS = List.of(
            "\\d{13,19}",
            "\\d{17}[\\dXx]",
            "1[3-9]\\d{9}",
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
            "(?i)(password|passwd|pwd)\\s*[=:]\\s*\\S+",
            "(?i)(authorization|token)\\s*[=:]\\s*\\S+",
            "(?i)bearer\\s+\\S+"
    );

    private static final String SYSTEM_REPLACEMENT = "secure-kit.log-guard.default-replacement";
    private static final String SYSTEM_PATTERNS = "secure-kit.log-guard.patterns";

    private final List<Pattern> patterns;
    private final List<String> skippedPatterns;
    private final String replacement;

    /**
     * Creates a pattern set from system properties, falling back to built-in defaults.
     *
     * <p>This constructor exists because logging frameworks instantiate extension classes without
     * a Spring context.</p>
     */
    public SensitivePatternSet() {
        this(resolveSystemPatterns(), System.getProperty(SYSTEM_REPLACEMENT));
    }

    /**
     * Creates a pattern set from Spring configuration.
     *
     * @param properties the configuration; {@code null} uses built-in defaults
     */
    public SensitivePatternSet(LogGuardProperties properties) {
        this(properties == null ? DEFAULT_PATTERNS : properties.getPatterns(),
                properties == null ? null : properties.getDefaultReplacement());
    }

    /**
     * Creates a pattern set from explicit expressions and replacement text.
     *
     * @param expressions the regular expressions; {@code null} or empty means no patterns
     * @param replacement the replacement text; {@code null} or empty falls back to {@code "*"}
     */
    public SensitivePatternSet(List<String> expressions, String replacement) {
        this.replacement = replacement == null || replacement.isEmpty() ? "*" : replacement;
        List<Pattern> compiled = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        if (expressions != null) {
            for (String expression : expressions) {
                if (expression == null || expression.isEmpty()) {
                    continue;
                }
                try {
                    compiled.add(Pattern.compile(expression));
                } catch (PatternSyntaxException ignored) {
                    skipped.add(expression);
                }
            }
        }
        this.patterns = Collections.unmodifiableList(compiled);
        this.skippedPatterns = Collections.unmodifiableList(skipped);
    }

    /**
     * Applies every pattern to the input, in order.
     *
     * @param input the log text; may be {@code null}
     * @return the guarded text, or {@code null} when the input is {@code null}
     */
    public String apply(String input) {
        if (input == null) {
            return null;
        }
        if (input.isEmpty() || patterns.isEmpty()) {
            return input;
        }
        String current = input;
        for (Pattern pattern : patterns) {
            try {
                current = pattern.matcher(current).replaceAll(Matcher.quoteReplacement(replacement));
            } catch (RuntimeException ignored) {
                // Guarding must never break logging. Keep the text produced so far.
            }
        }
        return current;
    }

    /**
     * Returns the replacement text used by this set.
     *
     * @return the replacement text; never {@code null} or empty
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * Returns the number of successfully compiled patterns.
     *
     * @return the compiled pattern count
     */
    public int size() {
        return patterns.size();
    }

    /**
     * Returns the expressions that could not be compiled.
     *
     * <p>The list is immutable and is never logged by this class.</p>
     *
     * @return the skipped expressions; never {@code null}
     */
    public List<String> getSkippedPatterns() {
        return skippedPatterns;
    }

    private static List<String> resolveSystemPatterns() {
        String configured = System.getProperty(SYSTEM_PATTERNS);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_PATTERNS;
        }
        List<String> values = new ArrayList<>();
        for (String part : configured.split(",")) {
            if (!part.isBlank()) {
                values.add(part.trim());
            }
        }
        return values.isEmpty() ? DEFAULT_PATTERNS : values;
    }
}
