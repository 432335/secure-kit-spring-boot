package io.github.user432335.securekit.benchmarks;

import io.github.user432335.securekit.core.MaskType;
import io.github.user432335.securekit.mask.core.MaskTemplate;
import io.github.user432335.securekit.mask.core.MaskerRegistry;
import io.github.user432335.securekit.mask.core.masker.BankCardMasker;
import io.github.user432335.securekit.mask.core.masker.EmailMasker;
import io.github.user432335.securekit.mask.core.masker.IdCardMasker;
import io.github.user432335.securekit.mask.core.masker.PhoneMasker;
import io.github.user432335.securekit.mask.core.masker.RegexMasker;
import io.github.user432335.securekit.mask.properties.MaskProperties;
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
public class MaskBenchmark {

    private MaskTemplate template;

    @Setup
    public void setup() {
        MaskProperties properties = new MaskProperties();
        template = new MaskTemplate(new MaskerRegistry(List.of(
                new PhoneMasker(properties),
                new IdCardMasker(properties),
                new BankCardMasker(properties),
                new EmailMasker(properties),
                new RegexMasker(properties))));
    }

    @Benchmark
    public String phone() {
        return template.mask(MaskType.PHONE, "13812345678");
    }

    @Benchmark
    public String email() {
        return template.mask(MaskType.EMAIL, "zhangsan@example.com");
    }

    @Benchmark
    public String custom() {
        return template.mask(MaskType.CUSTOM, "order-1234", "\\d{4}", "#");
    }
}
