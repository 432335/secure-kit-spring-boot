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
package io.github.user432335.securekit.mask.properties;

import io.github.user432335.securekit.core.MaskType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;

/**
 * Configuration for the secure-kit masking capability library.
 *
 * <p>The capability is disabled by default. Adding the starter does not change application
 * behaviour until {@code secure-kit.mask.enabled=true} is set and application code explicitly
 * calls {@code MaskTemplate}.</p>
 */
@ConfigurationProperties(prefix = "secure-kit.mask")
public class MaskProperties {

    /**
     * Whether the masking auto-configuration is active. Defaults to {@code false}.
     */
    private boolean enabled = false;

    /**
     * Replacement text used by the built-in maskers. A {@code null} or empty value falls back to
     * {@code "*"} at masking time.
     */
    private String defaultReplacement = "*";

    /**
     * Optional validation regular-expression overrides keyed by category. Invalid expressions are
     * ignored by the built-in maskers and the built-in rule is used instead.
     */
    private Map<MaskType, String> patterns = new EnumMap<>(MaskType.class);

    /**
     * Returns whether the masking auto-configuration is active.
     *
     * @return {@code true} when enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the masking auto-configuration is active.
     *
     * @param enabled the new value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the configured default replacement.
     *
     * @return the replacement text; may be {@code null}
     */
    public String getDefaultReplacement() {
        return defaultReplacement;
    }

    /**
     * Sets the configured default replacement.
     *
     * @param defaultReplacement the new replacement text; may be {@code null}
     */
    public void setDefaultReplacement(String defaultReplacement) {
        this.defaultReplacement = defaultReplacement;
    }

    /**
     * Returns the category-specific validation pattern overrides.
     *
     * @return the mutable configuration map; never {@code null} unless explicitly set to
     *         {@code null}
     */
    public Map<MaskType, String> getPatterns() {
        return patterns;
    }

    /**
     * Sets the category-specific validation pattern overrides.
     *
     * @param patterns the new map; may be {@code null}
     */
    public void setPatterns(Map<MaskType, String> patterns) {
        this.patterns = patterns;
    }
}
