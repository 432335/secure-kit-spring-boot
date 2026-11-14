# Security Policy

## Supported versions

| Version | Supported |
| --- | --- |
| `0.1.x` | Yes |
| `< 0.1` | No |

## Reporting a vulnerability

Please **do not open a public issue** for a security problem.

Use GitHub's private vulnerability reporting:

```text
https://github.com/432335/secure-kit-spring-boot/security/advisories/new
```

Include:

- affected version;
- Java and Spring Boot version;
- minimal reproduction;
- expected and actual behaviour;
- impact assessment;
- any suggested fix.

You will receive an acknowledgement as soon as possible. Valid reports will be fixed in a new release and credited unless you prefer to remain anonymous.

## Scope

In scope:

- masking bypasses or incorrect masking output;
- sensitive values leaking through audit events or logs;
- audit events being lost or reordered in a way that breaks the documented contract;
- log-guard bypasses caused by the built-in patterns;
- dependency or build-chain vulnerabilities;
- unsafe defaults.

Out of scope:

- authentication, authorization, login, JWT, OAuth2, or RBAC (not provided by this project);
- cryptography, hashing, or signing algorithms (not provided by this project);
- vulnerabilities in Spring Boot, Logback, Log4j2, or other upstream dependencies;
- social engineering;
- denial of service caused only by extreme configuration values.

## Security model

This project is a set of capability libraries, not a security boundary.

- It does not authenticate users.
- It does not authorize access to resources.
- It does not encrypt data at rest or in transit.
- It does not guarantee that every sensitive value is detected.
- mask is explicit; a missing call means no masking.
- log-guard is regex-based and can over-mask or under-mask.
- audit persistence and retention are the application's responsibility.

## Dependency security

The project keeps runtime dependencies minimal. Logging framework dependencies are optional. Please report a dependency vulnerability if it is reachable through this project's public API.
