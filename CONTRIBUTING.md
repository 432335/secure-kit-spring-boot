# Contributing

**English** | [简体中文](CONTRIBUTING.zh-CN.md)

Thanks for contributing to `secure-kit-spring-boot`.

## Requirements

- JDK 17
- Maven 3.9+
- Git

## Local build

```bash
mvn clean verify
```

Compile only:

```bash
mvn -DskipTests compile
```

## Pull requests

1. Fork the repository.
2. Create a focused branch from `main`.
3. Keep one pull request focused on one concern.
4. Add or update tests for behaviour changes.
5. Make sure `mvn verify` passes.
6. Update README, CHANGELOG, or Javadoc when needed.
7. Open the pull request with the motivation, behaviour, and test plan.

## Code guidelines

- Java 17.
- No Lombok, Guava, Commons Lang, MapStruct, or Fastjson.
- Keep modules loosely coupled. Do not add unnecessary hard dependencies.
- Public classes and public methods need Javadoc.
- New capabilities must remain default-off.
- Do not take over user control flow.
- A security capability failure must not break business calls.
- Never commit real secrets, tokens, passwords, or personal data.

## Tests

- JUnit 5, AssertJ, and `ApplicationContextRunner`.
- Cover normal paths, boundaries, nulls, and failure paths.
- For auto-configuration changes, verify default-off behaviour and user-bean back-off.

## Documentation

- README is for users: explain what it is and how to use it.
- Publishing steps are in [PUBLISHING.md](PUBLISHING.md).
- Changes are recorded in [CHANGELOG.md](CHANGELOG.md).

## Issues

Please include:

- version;
- JDK and Spring Boot version;
- minimal reproduction;
- expected and actual behaviour.

For security issues, use [SECURITY.md](SECURITY.md), not a public issue.

## License

By contributing, you agree that your contribution is licensed under Apache-2.0.
