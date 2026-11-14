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
package io.github.user432335.securekit.mask.autoconfigure;

import io.github.user432335.securekit.mask.core.MaskTemplate;
import io.github.user432335.securekit.mask.core.Masker;
import io.github.user432335.securekit.mask.core.MaskerRegistry;
import io.github.user432335.securekit.mask.core.masker.BankCardMasker;
import io.github.user432335.securekit.mask.core.masker.EmailMasker;
import io.github.user432335.securekit.mask.core.masker.IdCardMasker;
import io.github.user432335.securekit.mask.core.masker.PhoneMasker;
import io.github.user432335.securekit.mask.core.masker.RegexMasker;
import io.github.user432335.securekit.mask.properties.MaskProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Auto-configuration for the masking capability library.
 *
 * <p>The capability is disabled by default. It is active only when
 * {@code secure-kit.mask.enabled=true}. Enabling it registers the built-in maskers, an immutable
 * {@link MaskerRegistry} and the {@link MaskTemplate} facade; it still does not intercept
 * application code. Callers must invoke the facade explicitly.</p>
 *
 * <p>Every bean is declared with {@link ConditionalOnMissingBean}. A user-defined bean of the same
 * type automatically wins, so applications can replace individual maskers or the whole registry
 * without fighting the auto-configuration.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(MaskProperties.class)
@ConditionalOnProperty(prefix = "secure-kit.mask", name = "enabled", havingValue = "true")
public class MaskAutoConfiguration {

    /**
     * Creates the phone masker unless the application provides one.
     *
     * @param properties the masking properties
     * @return the phone masker
     */
    @Bean
    @ConditionalOnMissingBean
    public PhoneMasker phoneMasker(MaskProperties properties) {
        return new PhoneMasker(properties);
    }

    /**
     * Creates the identity-card masker unless the application provides one.
     *
     * @param properties the masking properties
     * @return the identity-card masker
     */
    @Bean
    @ConditionalOnMissingBean
    public IdCardMasker idCardMasker(MaskProperties properties) {
        return new IdCardMasker(properties);
    }

    /**
     * Creates the bank-card masker unless the application provides one.
     *
     * @param properties the masking properties
     * @return the bank-card masker
     */
    @Bean
    @ConditionalOnMissingBean
    public BankCardMasker bankCardMasker(MaskProperties properties) {
        return new BankCardMasker(properties);
    }

    /**
     * Creates the email masker unless the application provides one.
     *
     * @param properties the masking properties
     * @return the email masker
     */
    @Bean
    @ConditionalOnMissingBean
    public EmailMasker emailMasker(MaskProperties properties) {
        return new EmailMasker(properties);
    }

    /**
     * Creates the custom regular-expression masker unless the application provides one.
     *
     * @param properties the masking properties
     * @return the custom masker
     */
    @Bean
    @ConditionalOnMissingBean
    public RegexMasker regexMasker(MaskProperties properties) {
        return new RegexMasker(properties);
    }

    /**
     * Creates the immutable masker registry unless the application provides one.
     *
     * @param maskers all masker beans, including user-defined maskers
     * @return the registry
     */
    @Bean
    @ConditionalOnMissingBean
    public MaskerRegistry maskerRegistry(List<Masker> maskers) {
        return new MaskerRegistry(maskers);
    }

    /**
     * Creates the masking facade unless the application provides one.
     *
     * @param registry the masker registry
     * @return the facade
     */
    @Bean
    @ConditionalOnMissingBean
    public MaskTemplate maskTemplate(MaskerRegistry registry) {
        return new MaskTemplate(registry);
    }
}
