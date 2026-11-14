# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `CONTRIBUTING.md` contribution guide.
- `PUBLISHING.md` Maven Central publishing guide.

### Changed

- Restructured `README.md` around project overview, quick start, module details, FAQ, demo and contribution paths.

## [0.1.0] - 2026-11-14

### Added

#### secure-kit-core

- `MaskType` shared enum with `PHONE`, `ID_CARD`, `BANK_CARD`, `EMAIL` and `CUSTOM`.

#### secure-kit-mask

- Explicit `MaskTemplate` facade and immutable `MaskerRegistry`.
- Built-in maskers for phone numbers, identity-card numbers, bank-card numbers and email
  addresses.
- `RegexMasker` for caller-supplied `CUSTOM` expressions.
- `@Mask` metadata annotation; it does not intercept calls in 0.1.0.
- Optional Spring Boot auto-configuration, disabled by default, with per-bean back-off.

#### secure-kit-audit

- `@AuditLog` optional AOP notification point that never changes business results or swallows
  business exceptions.
- Immutable `AuditEvent` with argument and result snapshots.
- `AuditRepository` and `AuditUserProvider` SPI boundaries.
- Default DEBUG-only repository and configuration-backed operator provider.
- Bounded audit executor with daemon threads and a discard-and-count rejection policy.
- Optional mask-module cooperation through runtime bean detection; degrades to plaintext with one
  WARN when the mask capability is absent.

#### secure-kit-log-guard

- Immutable `SensitivePatternSet` with precompiled patterns and skipped-pattern reporting.
- Logback `SensitiveMessageConverter`.
- Log4j2 `SensitiveRewritePolicy` that guards message and context data.
- Optional Spring Boot auto-configuration, disabled by default.
- No automatic modification of user logging configuration files.

#### Documentation and build

- Apache-2.0 license, README, changelog, CI workflow and Maven release profile.
