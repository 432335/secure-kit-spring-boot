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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstration application for the secure-kit capability libraries.
 *
 * <p>The application enables the mask and audit capabilities in {@code application.yml}. It is a
 * small integration surface for the libraries; it is not a security framework or a production
 * service.</p>
 */
@SpringBootApplication
public class DemoApplication {

    /**
     * Starts the demonstration application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
