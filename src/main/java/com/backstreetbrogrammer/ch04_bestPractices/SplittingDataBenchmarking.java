package com.backstreetbrogrammer.ch04_bestPractices;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class SplittingDataBenchmarking {

    @Param({"1000000"})
    private int N;

    private final List<Integer> arrayListOfNumbers = new ArrayList<>();
    private final List<Integer> linkedListOfNumbers = new LinkedList<>();

    @Setup
    public void setup() {
        IntStream.rangeClosed(1, N).forEach(i -> {
            arrayListOfNumbers.add(i);
            linkedListOfNumbers.add(i);
        });
    }

    @Benchmark
    public double sum_arrayList_sequential() {
        return arrayListOfNumbers.stream().reduce(0, Integer::sum);
    }

    @Benchmark
    public double sum_arrayList_parallel() {
        return arrayListOfNumbers.parallelStream().reduce(0, Integer::sum);
    }

    @Benchmark
    public double sum_linkedList_sequential() {
        return linkedListOfNumbers.stream().reduce(0, Integer::sum);
    }

    @Benchmark
    public double sum_linkedList_parallel() {
        return linkedListOfNumbers.parallelStream().reduce(0, Integer::sum);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SplittingDataBenchmarking.class.getName())
                .build();
        new Runner(opt).run();
    }
}
