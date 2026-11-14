# Benchmarks

Standalone JMH project. It is intentionally not part of the main Maven reactor.

## Run

```bash
mvn -f benchmarks/pom.xml clean package
java -jar benchmarks/target/benchmarks.jar
```

Run a single benchmark:

```bash
java -jar benchmarks/target/benchmarks.jar MaskBenchmark
```

## What is measured

- `MaskBenchmark`: phone, email, and custom-regex masking through `MaskTemplate`.
- `LogGuardBenchmark`: one mixed log line through `SensitivePatternSet`.

Benchmarks are indicative, not a guarantee. Run them on the target JVM and hardware before making performance claims.
