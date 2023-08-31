package com.backstreetbrogrammer.ch02_performanceGains;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AutoboxingBenchmarking {

    @Param({"100000"})
    private int N;

    private int[] arrayOfInts;
    private Integer[] arrayOfIntegers;

    @Setup
    public void createArrayOfInts() {
        arrayOfInts = new int[N];
        for (int i = 0; i < N; i++) {
            arrayOfInts[i] = 3 * i;
        }
    }

    @Setup
    public void createArrayOfIntegers() {
        arrayOfIntegers = new Integer[N];
        for (int i = 0; i < N; i++) {
            arrayOfIntegers[i] = 3 * i;
        }
    }

    @Benchmark
    public int calculate_sum_of_ints() {
        int sum = 0;
        for (int i = 0; i < arrayOfInts.length; i++) {
            sum += i * 7;
        }
        return sum;
    }

    @Benchmark
    public int calculate_sum_of_integers() {
        Integer sum = 0;
        for (int i = 0; i < arrayOfIntegers.length; i++) {
            sum += i * 7;
        }
        return sum;
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(AutoboxingBenchmarking.class.getName())
                .build();
        new Runner(opt).run();
    }

}
