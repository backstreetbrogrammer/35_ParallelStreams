package com.backstreetbrogrammer.ch04_bestPractices;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class MergingResultsBenchmarking {

    @Param({"1000000"})
    private int N;

    private final List<Integer> arrayListOfNumbers = new ArrayList<>();

    @Setup
    public void setup() {
        IntStream.rangeClosed(1, N).forEach(arrayListOfNumbers::add);
    }

    @Benchmark
    public double sum_arrayList_sequential() {
        return arrayListOfNumbers.stream().reduce(0, Integer::sum);
    }

    @Benchmark
    public double sum_arrayList_parallel() {
        return arrayListOfNumbers.stream().parallel().reduce(0, Integer::sum);
    }

    @Benchmark
    public Set<Integer> collect_arrayList_sequential() {
        return arrayListOfNumbers.stream().collect(Collectors.toSet());
    }

    @Benchmark
    public Set<Integer> collect_arrayList_parallel() {
        return arrayListOfNumbers.stream().parallel().collect(Collectors.toSet());
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(MergingResultsBenchmarking.class.getName())
                .build();
        new Runner(opt).run();
    }
}
