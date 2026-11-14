# Comparison

This document is intentionally conservative. It compares capabilities, not marketing claims.

| Capability | secure-kit | Manual regex in business code | Spring Security | Logback/Log4j2 masking appender | Audit framework |
| --- | --- | --- | --- | --- | --- |
| Explicit value masking | Yes | Yes | No | No | Usually no |
| Built-in phone / ID / bank / email rules | Yes | You write and test them | No | Depends | No |
| Audit events | Yes | No | No | No | Yes |
| Pluggable audit storage | Yes | No | No | No | Usually yes |
| Log output redaction | Yes | No | No | Yes | No |
| Default off | Yes | N/A | N/A | Usually no | Depends |
| No filter/interceptor takeover | Yes | Yes | No (by design) | Yes | Depends |
| Authentication / authorization | No | No | Yes | No | No |
| Cryptography / signing | No | No | Partial | No | No |
| Runtime dependencies | Spring Boot + optional logging APIs | None | Spring ecosystem | Logging framework | Varies |

## secure-kit

Use it when you want:

- Explicit masking at the point where data leaves your domain.
- A small audit event model with your own storage.
- Optional log output redaction at the appender level.
- Capabilities disabled by default and independently adoptable.

Do not use it as a replacement for authentication, authorization, or cryptography.

## Manual regex

Use manual regex when:

- You have only one or two very specific fields.
- You want zero additional dependencies.

Trade-offs:

- Rules are duplicated across services.
- Tests are often missing.
- Log output is easy to forget.
- Audit and log redaction remain separate concerns.

## Spring Security

Spring Security is the right choice for authentication, authorization, session management, and filter-chain security.

It is not a masking library, an audit repository, or a log redaction tool. This project is complementary.

## Logback/Log4j2 masking appenders

Use a logging appender when:

- You only need log output redaction.
- You already have a logging configuration pipeline.

Trade-offs:

- It does not help when a value is returned from an API or written to a database.
- Broad regexes can over-mask business data.
- Configuration is framework-specific.

## Audit frameworks

Use a dedicated audit framework when:

- Audit is the primary concern.
- You need a large schema, query UI, or compliance workflow.

This project stays small: an annotation, an immutable event, a listener, and a repository SPI.

## Summary

`secure-kit-spring-boot` is not the most powerful option in any single category. Its value is the combination:

- explicit masking;
- audit events;
- log output guarding;
- default off;
- no framework takeover;
- small, replaceable boundaries.
