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
package io.github.user432335.securekit.core;

/**
 * Built-in masking categories understood by the secure-kit masking capability.
 *
 * <p>This enum is shared by the core module and the mask/audit capability libraries. It contains
 * no behaviour: a category only describes the shape of a value. The actual masking rules live in
 * the mask library.</p>
 *
 * <p>{@link #CUSTOM} means that the caller supplies a regular expression through the mask API. It
 * is intentionally not a catch-all category for arbitrary business data.</p>
 */
public enum MaskType {

    /** Mainland China mobile phone number. */
    PHONE,

    /** Mainland China resident identity card number (18 characters). */
    ID_CARD,

    /** Bank card number with 13 to 19 digits. */
    BANK_CARD,

    /** Email address. */
    EMAIL,

    /** Caller-supplied regular expression. */
    CUSTOM
}
