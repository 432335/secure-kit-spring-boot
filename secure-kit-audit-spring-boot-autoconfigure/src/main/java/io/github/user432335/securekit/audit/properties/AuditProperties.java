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
package io.github.user432335.securekit.audit.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the secure-kit audit capability library.
 *
 * <p>The capability is disabled by default. Enabling it registers an optional AOP notification
 * point and an event listener; it does not change application control flow.</p>
 */
@ConfigurationProperties(prefix = "secure-kit.audit")
public class AuditProperties {

    /**
     * Whether the audit auto-configuration is active. Defaults to {@code false}.
     */
    private boolean enabled = false;

    /**
     * Whether audit event handling runs on the bounded audit executor. Defaults to {@code true}.
     */
    private boolean async = true;

    /**
     * Operator name used when no {@code AuditUserProvider} supplies one. Defaults to
     * {@code "anonymous"}.
     */
    private String defaultOperator = "anonymous";

    /**
     * Whether the listener attempts to mask operator, target and summary values before saving.
     * Defaults to {@code true}.
     */
    private boolean maskBeforeSave = true;

    /**
     * Core pool size of the audit executor. Defaults to {@code 1}.
     */
    private int asyncCorePoolSize = 1;

    /**
     * Maximum pool size of the audit executor. Defaults to {@code 4}.
     */
    private int asyncMaxPoolSize = 4;

    /**
     * Bounded queue capacity of the audit executor. Defaults to {@code 256}.
     */
    private int asyncQueueCapacity = 256;

    /**
     * Returns whether the audit capability is active.
     *
     * @return {@code true} when enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the audit capability is active.
     *
     * @param enabled the new value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether audit event handling is asynchronous.
     *
     * @return {@code true} when asynchronous handling is requested
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Sets whether audit event handling is asynchronous.
     *
     * @param async the new value
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Returns the fallback operator name.
     *
     * @return the fallback operator name; may be {@code null}
     */
    public String getDefaultOperator() {
        return defaultOperator;
    }

    /**
     * Sets the fallback operator name.
     *
     * @param defaultOperator the new fallback operator name; may be {@code null}
     */
    public void setDefaultOperator(String defaultOperator) {
        this.defaultOperator = defaultOperator;
    }

    /**
     * Returns whether values are masked before the repository is called.
     *
     * @return {@code true} when pre-save masking is requested
     */
    public boolean isMaskBeforeSave() {
        return maskBeforeSave;
    }

    /**
     * Sets whether values are masked before the repository is called.
     *
     * @param maskBeforeSave the new value
     */
    public void setMaskBeforeSave(boolean maskBeforeSave) {
        this.maskBeforeSave = maskBeforeSave;
    }

    /**
     * Returns the audit executor core pool size.
     *
     * @return the core pool size
     */
    public int getAsyncCorePoolSize() {
        return asyncCorePoolSize;
    }

    /**
     * Sets the audit executor core pool size.
     *
     * @param asyncCorePoolSize the new core pool size
     */
    public void setAsyncCorePoolSize(int asyncCorePoolSize) {
        this.asyncCorePoolSize = asyncCorePoolSize;
    }

    /**
     * Returns the audit executor maximum pool size.
     *
     * @return the maximum pool size
     */
    public int getAsyncMaxPoolSize() {
        return asyncMaxPoolSize;
    }

    /**
     * Sets the audit executor maximum pool size.
     *
     * @param asyncMaxPoolSize the new maximum pool size
     */
    public void setAsyncMaxPoolSize(int asyncMaxPoolSize) {
        this.asyncMaxPoolSize = asyncMaxPoolSize;
    }

    /**
     * Returns the audit executor queue capacity.
     *
     * @return the bounded queue capacity
     */
    public int getAsyncQueueCapacity() {
        return asyncQueueCapacity;
    }

    /**
     * Sets the audit executor queue capacity.
     *
     * @param asyncQueueCapacity the new bounded queue capacity
     */
    public void setAsyncQueueCapacity(int asyncQueueCapacity) {
        this.asyncQueueCapacity = asyncQueueCapacity;
    }
}
