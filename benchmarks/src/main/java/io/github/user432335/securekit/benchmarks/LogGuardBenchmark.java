package io.github.user432335.securekit.benchmarks;

import io.github.user432335.securekit.logguard.core.SensitivePatternSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class LogGuardBenchmark {

    private SensitivePatternSet patternSet;

    @Setup
    public void setup() {
        patternSet = new SensitivePatternSet(List.of(
                "1[3-9]\\d{9}",
                "\\d{17}[\\dXx]",
                "\\d{13,19}",
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        ), "*");
    }

    @Benchmark
    public String mixedLine() {
        return patternSet.apply("user=alice phone=13812345678 order=1234567890123");
    }
}
