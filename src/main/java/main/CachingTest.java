package main;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(
    jvmArgs = {
        "-XX:-TieredCompilation",
        "-Xms32g"
})
public class CachingTest {

    private static final int NUM_LOOKUPS = 1_000_000;
    private static final int MAP_SIZE = 100_000_000;
    private final int[] indexes = new int[NUM_LOOKUPS];
    private Map<Integer, Double> cache;
    private Int2DoubleOpenHashMap primitiveCache;

    @Benchmark
    public void f1(Blackhole blackhole) {
        for (int i : indexes) {
            blackhole.consume(f1(i));
        }
    }

    @Benchmark
    public void f2(Blackhole blackhole) {
        for (int i : indexes) {
            blackhole.consume(f2(i));
        }
    }

    @Benchmark
    public void hashMap(Blackhole blackhole) {
        for (int i : indexes) {
            blackhole.consume(cache.get(i));
        }
    }

    @Benchmark
    public void int2DoubleHashMap(Blackhole blackhole) {
        for (int i : indexes) {
            blackhole.consume(primitiveCache.get(i));
        }
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        Random r = new Random();

        for (int i = 0; i < NUM_LOOKUPS; i++) {
            indexes[i] = r.nextInt(MAP_SIZE);
        }
    }

    @Setup(Level.Trial)
    public void setup() {
        cache = new HashMap<>(MAP_SIZE);
        primitiveCache = new Int2DoubleOpenHashMap(MAP_SIZE);

        for (int i = 0; i < MAP_SIZE; i++) {
            cache.put(i, f1(i));
            primitiveCache.put(i, f1(i));
        }
    }

    private double f1(double i) {
        double d = (i * i + 5*i - 99*i) * (i * 76.0 / (i*4 + 1));
        return ((int) d) ^ 7 ^ (int) (i*i + i/7);
    }

    private double f2(double i) {
        return FastMath.sqrt(i) * FastMath.tan(i) * i / (98 * i);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(CachingTest.class.getSimpleName())
            .build();

        new Runner(options).run();
    }
}
