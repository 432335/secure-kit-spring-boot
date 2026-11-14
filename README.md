# secure-kit-spring-boot

**English** | [简体中文](README.zh-CN.md)

[![CI](https://github.com/432335/secure-kit-spring-boot/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/432335/secure-kit-spring-boot/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.432335/secure-kit-core?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.432335/secure-kit-core)

## 1. Overview

A set of default-off, independently adoptable Spring Boot security capability libraries: explicit masking, audit events, and log output guarding.

## 2. 30-second overview

### Before

```java
String phone = user.getPhone();
return phone;
```

### After

```java
String phone = maskTemplate.mask(MaskType.PHONE, user.getPhone());
return phone;
```

### Audit before

```java
public Order queryOrder(String orderId) {
    return orderService.query(orderId);
}
```

### Audit after

```java
@AuditLog(action = "query-order", targetIdExpression = "#p0")
public Order queryOrder(String orderId) {
    return orderService.query(orderId);
}
```

### Log output guarding

```text
# before
2026-01-01 10:00:00 INFO  c.e.OrderService - phone=13812345678

# after configuring log-guard
2026-01-01 10:00:00 INFO  c.e.OrderService - phone=*
```

## 3. Library, not framework

This is a **library**, not a framework.

- You own the control flow: nothing happens unless you call it.
- It does not take over your application structure.
- It is disabled by default: adding a dependency does not change behaviour.
- It does not do authentication, authorization, login, JWT, OAuth2, RBAC, or API-key validation.
- It does not implement cryptography, hashing, or signing algorithms.

Relationship with Spring Security: **complementary, not a replacement**.

- Spring Security handles authentication, authorization, and the filter chain.
- This project provides masking, audit events, and log output guarding.
- Implement `AuditUserProvider` if you need the current principal from Spring Security.

See [docs/comparison.md](docs/comparison.md) for a fair comparison with manual regex, Spring Security, logback masking, and audit frameworks.

## 4. Modules

| Module | Purpose | Type | Default | Coordinates |
| --- | --- | --- | --- | --- |
| `secure-kit-core` | Shared `MaskType` enum | Shared library | N/A | `io.github.432335:secure-kit-core` |
| `secure-kit-mask-spring-boot-starter` | Explicit masking | Pure library | `false` | `io.github.432335:secure-kit-mask-spring-boot-starter` |
| `secure-kit-audit-spring-boot-starter` | Audit events | Capability library + optional AOP | `false` | `io.github.432335:secure-kit-audit-spring-boot-starter` |
| `secure-kit-log-guard-spring-boot-starter` | Log output guarding | Logging extension | `false` | `io.github.432335:secure-kit-log-guard-spring-boot-starter` |
| `secure-kit-demo` | Demo and integration test | Demo app | N/A | Not published for new releases |

### Why disabled by default

- Adding a dependency should not change existing business behaviour.
- Security capabilities should be explicitly enabled, not silently applied.
- Avoid accidental over-masking, log overhead, and audit queue usage.
- Makes incremental adoption possible.

## 5. Quick start

### Requirements

- JDK 17
- Maven 3.9+
- Spring Boot 3.2.x

### Option A: install from source

```bash
mvn clean install
```

Then use version `0.1.0` in your application.

### Option B: Maven Central

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-mask-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

> Maven coordinates use `io.github.432335`; Java packages use `io.github.user432335.securekit`. They do not have to match.

## 6. Capability details

### 6.1 mask: explicit masking

#### Dependency

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-mask-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Enable

```yaml
secure-kit:
  mask:
    enabled: true
```

#### API order

`MaskTemplate` has three overloads:

```java
mask(MaskType type, String original)
mask(MaskType type, String original, String replacement)
mask(MaskType type, String original, String pattern, String replacement)
```

There is no `mask(type, original, pattern)` overload. A custom regex requires all four arguments.

#### Usage

```java
import io.github.user432335.securekit.core.MaskType;
import io.github.user432335.securekit.mask.core.MaskTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final MaskTemplate maskTemplate;

    public UserService(MaskTemplate maskTemplate) {
        this.maskTemplate = maskTemplate;
    }

    public String phone(String raw) {
        return maskTemplate.mask(MaskType.PHONE, raw);
    }

    public String email(String raw) {
        return maskTemplate.mask(MaskType.EMAIL, raw);
    }

    public String custom(String raw) {
        return maskTemplate.mask(MaskType.CUSTOM, raw, "\\d{4}", "#");
    }
}
```

`@Mask` is metadata only in 0.1.0. It does not intercept fields, parameters, or methods, and it does not change Jackson serialization. Call `MaskTemplate` explicitly.

#### Built-in types

| Type | Input example | Default output |
| --- | --- | --- |
| `PHONE` | `13812345678` | `138****5678` |
| `ID_CARD` | `110101199001011234` | `110101********1234` |
| `BANK_CARD` | `6222021234567890123` | `6222***********0123` |
| `EMAIL` | `zhangsan@example.com` | `z*******@example.com` |
| `CUSTOM` | `order-1234` + `\d{4}` | `order-*` |

### 6.2 audit: audit events

#### Dependency

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-audit-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Enable

```yaml
secure-kit:
  audit:
    enabled: true
    async: true
    default-operator: anonymous
    mask-before-save: true
```

#### Usage

```java
import io.github.user432335.securekit.audit.annotation.AuditLog;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @AuditLog(action = "query-order", targetIdExpression = "#p0")
    public String queryOrder(String orderId) {
        return "order:" + orderId;
    }
}
```

#### Use your own storage

The default implementation only writes a DEBUG line. Provide your own `AuditRepository` bean in production:

```java
import io.github.user432335.securekit.audit.event.AuditEvent;
import io.github.user432335.securekit.audit.spi.AuditRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuditConfig {

    @Bean
    public AuditRepository auditRepository() {
        return new AuditRepository() {
            @Override
            public void save(AuditEvent event) {
                // write to your database, message queue, or audit platform
            }
        };
    }
}
```

#### Current operator

```java
@Bean
public AuditUserProvider auditUserProvider() {
    return () -> "alice";
}
```

#### Notes

- audit does not require mask.
- When mask is present, values are masked before saving.
- When mask is absent, events are stored as plaintext and one WARN is emitted.
- The audit executor is bounded; rejected tasks are discarded without blocking or throwing into business threads.
- The default repository does not persist anything. Replace it in production.

### 6.3 log-guard: log output guarding

#### Dependency

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-log-guard-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Enable

```yaml
secure-kit:
  log-guard:
    enabled: true
    default-replacement: "*"
```

#### Logback

```xml
<configuration>
    <conversionRule conversionWord="smsg"
                    converterClass="io.github.user432335.securekit.logguard.logback.SensitiveMessageConverter"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %smsg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

#### Log4j2

```xml
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d %-5p %c - %m%n"/>
        </Console>
        <Rewrite name="Rewrite">
            <SensitiveRewritePolicy/>
            <AppenderRef ref="Console"/>
        </Rewrite>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Rewrite"/>
        </Root>
    </Loggers>
</Configuration>
```

#### Notes

- The module never modifies `logback.xml` or `log4j2.xml`.
- Converters and policies are created by the logging framework and cannot use Spring injection.
- Optional configuration is read from system properties:

```text
-Dsecure-kit.log-guard.default-replacement=#
-Dsecure-kit.log-guard.patterns=1[3-9]\d{9},\d{17}[\dXx]
```

## 7. Configuration

### mask

| Property | Default | Description |
| --- | --- | --- |
| `secure-kit.mask.enabled` | `false` | Enable the capability |
| `secure-kit.mask.default-replacement` | `*` | Replacement text |
| `secure-kit.mask.patterns.<TYPE>` | empty | Override built-in validation regex |

### audit

| Property | Default | Description |
| --- | --- | --- |
| `secure-kit.audit.enabled` | `false` | Enable the capability |
| `secure-kit.audit.async` | `true` | Handle events asynchronously |
| `secure-kit.audit.default-operator` | `anonymous` | Fallback operator |
| `secure-kit.audit.mask-before-save` | `true` | Mask before saving |
| `secure-kit.audit.async-core-pool-size` | `1` | Core pool size |
| `secure-kit.audit.async-max-pool-size` | `4` | Maximum pool size |
| `secure-kit.audit.async-queue-capacity` | `256` | Bounded queue capacity |

### log-guard

| Property | Default | Description |
| --- | --- | --- |
| `secure-kit.log-guard.enabled` | `false` | Enable the capability |
| `secure-kit.log-guard.default-replacement` | `*` | Replacement text |
| `secure-kit.log-guard.patterns` | 7 built-in patterns | Sensitive regex list |

## 8. Customization

### Custom Masker

```java
@Bean
public Masker employeeNoMasker() {
    return new Masker() {
        @Override
        public MaskType supportedType() {
            return MaskType.CUSTOM;
        }

        @Override
        public String mask(String original, String pattern) {
            return original == null ? null : original.replaceAll("\\d{6}", "******");
        }
    };
}
```

### Custom AuditRepository

```java
@Bean
public AuditRepository auditRepository() {
    return event -> {
        // must be thread-safe; keep it fast
    };
}
```

### Custom AuditUserProvider

```java
@Bean
public AuditUserProvider auditUserProvider() {
    return () -> "alice";
}
```

## 9. FAQ

### Why did nothing happen after adding the dependency?

All modules are disabled by default. Set `secure-kit.<module>.enabled=true`.

### Why does `@Mask` not mask automatically?

In 0.1.0, `@Mask` is metadata only. Call `MaskTemplate` explicitly.

### Where does audit store events by default?

It only writes a DEBUG line. Provide your own `AuditRepository` in production.

### Can audit work without mask?

Yes. Events are stored as plaintext and one WARN is emitted.

### log-guard is enabled but has no effect?

You must also reference `SensitiveMessageConverter` in `logback.xml` or `SensitiveRewritePolicy` in `log4j2.xml`.

### Does it conflict with Spring Security?

No. This project does not register any Filter or Interceptor and does not take over `SecurityContext`. It can be used alongside Spring Security.

### What is the performance overhead?

mask is plain string/regex matching. audit is asynchronous with a bounded queue. log-guard adds one regex replace per log line; enable it only on the appenders that need it.

### Why is audit not persistent by default?

Persistence choices vary too much (JDBC, file, MQ, audit platform). The default only writes DEBUG; implement `AuditRepository` in production.

### Will log-guard over-mask numeric order IDs?

Yes. The bank-card regex `\d{13,19}` is broad. Override `secure-kit.log-guard.patterns` with a precise business rule.

### Can it do authentication, authorization, or JWT?

No. Use Spring Security or another dedicated solution.

### Does it support Spring Boot 2.x?

No. Spring Boot 3.2.x is supported.

### The dependency is not on Central. What should I do?

Run `mvn clean install` in this repository and use the locally installed `0.1.0`.

## 10. Boundaries and known limitations

- No authentication, authorization, login, JWT, OAuth2, or RBAC.
- No cryptography, hashing, or signing algorithms.
- mask is explicit; forgetting to call it means no masking.
- audit does not persist by default; the bounded queue may drop events under saturation.
- log-guard regex can over-mask numeric business fields. Override the patterns.
- log-guard is an output guard, not a DLP system.
- `errorMessage` in audit events is not masked.
- Complementary to Spring Security, not a replacement.

## 11. Compatibility

- Java 17
- Spring Boot 3.2.x
- Maven 3.9+
- Spring Boot 3.x `AutoConfiguration.imports`
- Not compatible with Spring Boot 2.x
- Build: `mvn verify`

## 12. Demo

`secure-kit-demo` is a minimal integration example for mask + audit.

```bash
# run the demo integration test
mvn -pl secure-kit-demo -am test

# start the demo
mvn -pl secure-kit-demo -am spring-boot:run
```

The demo is for capability validation; it does not expose pages or REST endpoints.

## 13. Roadmap

### 0.2

- Optional Jackson serialization integration for `@Mask`.
- More built-in masking types.
- Optional JDBC `AuditRepository`.
- More complete release and compatibility documentation.

Performance benchmarks live in [benchmarks/README.md](benchmarks/README.md).

If this project helps you, please consider giving it a Star.

## 14. Contributing

Issues and pull requests are welcome.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) and make sure:

```bash
mvn verify
```

passes.

## 15. License

Apache License 2.0. See [LICENSE](LICENSE).
