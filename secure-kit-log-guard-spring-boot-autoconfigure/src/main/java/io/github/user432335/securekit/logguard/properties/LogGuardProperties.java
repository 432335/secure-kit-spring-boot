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
package io.github.user432335.securekit.logguard.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration for the secure-kit log output guarding capability library.
 *
 * <p>The capability is disabled by default. Even when enabled, this library does not modify the
 * application's {@code logback.xml} or {@code log4j2.xml}; the application must reference the
 * provided converter or rewrite policy explicitly.</p>
 *
 * <p>The bank-card and identity-card expressions are intentionally broad and can match pure
 * numeric business fields. Override {@link #getPatterns()} when those false positives matter.</p>
 */
@ConfigurationProperties(prefix = "secure-kit.log-guard")
public class LogGuardProperties {

    /**
     * Whether the log-guard auto-configuration is active. Defaults to {@code false}.
     */
    private boolean enabled = false;

    /**
     * Replacement text used for every sensitive match. A {@code null} or empty value falls back to
     * {@code "*"}.
     */
    private String defaultReplacement = "*";

    /**
     * Regular expressions applied to log text. The defaults cover phone numbers, identity-card
     * numbers, bank-card numbers, email addresses, password-like key/value pairs, authorization
     * headers, tokens and bearer credentials. Numeric defaults are ordered longest-first so that
     * an identity-card number is not partially consumed by the phone-number rule.
     */
    private List<String> patterns = List.of(
            "\\d{13,19}",
            "\\d{17}[\\dXx]",
            "1[3-9]\\d{9}",
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
            "(?i)(password|passwd|pwd)\\s*[=:]\\s*\\S+",
            "(?i)(authorization|token)\\s*[=:]\\s*\\S+",
            "(?i)bearer\\s+\\S+"
    );

    /**
     * Returns whether the log-guard auto-configuration is active.
     *
     * @return {@code true} when enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the log-guard auto-configuration is active.
     *
     * @param enabled the new value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the replacement text.
     *
     * @return the replacement text; may be {@code null}
     */
    public String getDefaultReplacement() {
        return defaultReplacement;
    }

    /**
     * Sets the replacement text.
     *
     * @param defaultReplacement the new replacement text; may be {@code null}
     */
    public void setDefaultReplacement(String defaultReplacement) {
        this.defaultReplacement = defaultReplacement;
    }

    /**
     * Returns the sensitive regular expressions.
     *
     * @return the configured expressions; may be {@code null} if explicitly set that way
     */
    public List<String> getPatterns() {
        return patterns;
    }

    /**
     * Sets the sensitive regular expressions.
     *
     * @param patterns the new expressions; may be {@code null}
     */
    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }
}
