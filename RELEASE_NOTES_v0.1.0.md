# secure-kit-spring-boot 0.1.0

First public release.

A set of default-off, independently adoptable Spring Boot security capability libraries: explicit masking, audit events, and log output guarding.

## Highlights

- **mask**: explicit `MaskTemplate` API with built-in phone, ID-card, bank-card, email, and custom-regex maskers.
- **audit**: optional `@AuditLog` notification point, immutable `AuditEvent`, pluggable `AuditRepository`, and bounded asynchronous executor.
- **log-guard**: Logback `SensitiveMessageConverter` and Log4j2 `SensitiveRewritePolicy` for log output redaction.
- **default off**: adding a starter does not change application behaviour.
- **no framework takeover**: no filters, no interceptors, no authentication, no authorization.

## Maven coordinates

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-mask-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-audit-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.432335</groupId>
    <artifactId>secure-kit-log-guard-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Compatibility

- Java 17
- Spring Boot 3.2.x
- Maven 3.9+

## Known limitations

- `@Mask` is metadata only in 0.1.0; call `MaskTemplate` explicitly.
- audit does not persist events by default; provide an `AuditRepository`.
- The bounded audit queue may drop events under saturation.
- log-guard regexes can over-mask numeric business fields; override the patterns.
- No authentication, authorization, JWT, OAuth2, RBAC, or cryptography.

## Verification

```bash
mvn clean verify
```

## Links

- Repository: https://github.com/432335/secure-kit-spring-boot
- English README: [README.md](README.md)
- Chinese README: [README.zh-CN.md](README.zh-CN.md)
- Comparison: [docs/comparison.md](docs/comparison.md)
- Security policy: [SECURITY.md](SECURITY.md)
- Contributing: [CONTRIBUTING.md](CONTRIBUTING.md)
