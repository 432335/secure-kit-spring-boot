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
package io.github.user432335.securekit.demo;

import io.github.user432335.securekit.audit.annotation.AuditLog;
import org.springframework.stereotype.Service;

/**
 * Small business service used by the demo and integration test.
 *
 * <p>The method is ordinary application code. The audit module observes it only because of the
 * {@link AuditLog} annotation, and the mask module is called by the audit listener rather than
 * being woven into this class.</p>
 */
@Service
public class DemoService {

    /**
     * Returns a synthetic user label for a phone number.
     *
     * @param phone the phone number; may be {@code null}
     * @return a synthetic label
     */
    @AuditLog(action = "query-user", targetIdExpression = "#p0")
    public String queryUser(String phone) {
        return "user:" + phone;
    }
}
