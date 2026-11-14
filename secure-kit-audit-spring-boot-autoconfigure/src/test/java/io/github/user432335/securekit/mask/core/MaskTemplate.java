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
 * Test-only stand-in for the mask module facade.
 *
 * <p>The audit module deliberately has no Maven dependency on the mask module. This class uses the
 * same fully qualified name so the audit listener's runtime detection path can be exercised
 * without adding that dependency to production code.</p>
 */
public class MaskTemplate {

    /**
     * Masks a value with a deliberately simple test rule.
     *
     * @param type     the category
     * @param original the original value
     * @return the masked value
     */
    public String mask(MaskType type, String original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        return switch (type) {
            case PHONE -> original.replaceAll("1[3-9]\\d{9}", "138****5678");
            case ID_CARD -> original.replaceAll("\\d{17}[\\dXx]", "110101********1234");
            case BANK_CARD -> original.replaceAll("\\d{13,19}", "6222********1234");
            case EMAIL -> original.replaceAll("([^@\\s])[^@\\s]*@", "$1***@");
            case CUSTOM -> original;
        };
    }
}
