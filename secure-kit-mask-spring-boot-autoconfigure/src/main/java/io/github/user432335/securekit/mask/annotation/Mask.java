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
package io.github.user432335.securekit.mask.annotation;

import io.github.user432335.securekit.core.MaskType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata marker for values that may need masking.
 *
 * <p><strong>0.1.0 behaviour:</strong> this annotation is metadata only. The library does not
 * intercept annotated fields, parameters or methods, and it does not change serialization. Call
 * {@code MaskTemplate} explicitly at the point where masking is required. Automatic Jackson
 * integration is intentionally deferred to a later release.</p>
 *
 * <p>The annotation has no runtime effect by itself. This keeps the mask module a library rather
 * than a framework that takes over application control flow.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mask {

    /**
     * Logical category of the annotated value.
     *
     * @return the masking category, never {@code null}
     */
    MaskType type() default MaskType.PHONE;

    /**
     * Optional regular expression for {@link MaskType#CUSTOM}.
     *
     * <p>Ignored for built-in categories in 0.1.0. An empty value means "not supplied".</p>
     *
     * @return the custom regular expression, or an empty string
     */
    String pattern() default "";

    /**
     * Optional replacement text.
     *
     * <p>An empty value means "use the configured default replacement". The annotation is metadata
     * in 0.1.0, so this value is not applied automatically.</p>
     *
     * @return the replacement text, or an empty string
     */
    String replacement() default "";
}
