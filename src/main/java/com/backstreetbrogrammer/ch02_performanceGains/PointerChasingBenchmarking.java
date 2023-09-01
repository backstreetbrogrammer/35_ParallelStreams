package com.backstreetbrogrammer.ch02_performanceGains;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Warmup(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class PointerChasingBenchmarking {

    @Param({"100000"})
    private int N;

    private ArrayList<Integer> arrayList = new ArrayList<>();
    private LinkedList<Integer> linkedList = new LinkedList<>();
    private LinkedList<Integer> shuffledLinkedList = new LinkedList<>();
    private LinkedList<Integer> scatteredLinkedList = new LinkedList<>();

    @Setup
    public void createArrayList() {
        arrayList = IntStream.range(0, N)
                             .map(i -> i * 3)
                             .boxed()
                             .collect(Collectors.toCollection(ArrayList::new));
    }

    @Setup
    public void createLinkedList() {
        linkedList = IntStream.range(0, N)
                              .map(i -> i * 3)
                              .boxed()
                              .collect(Collectors.toCollection(LinkedList::new));
    }

    @Setup
    public void createShuffledLinkedList() {
        shuffledLinkedList = new LinkedList<>();
        for (int i = 1; i < N + 1; i++) {
            shuffledLinkedList.add(i * 3);
        }
        Collections.shuffle(shuffledLinkedList, new Random(314159L));
    }

    @Setup
    public void createScatteredLinkedList() {
        scatteredLinkedList = new LinkedList<>();
        for (int i = 1; i < N + 1; i++) {
            scatteredLinkedList.add(i * 3);
            for (int j = 0; j < 100; j++) {
                scatteredLinkedList.add(0);
            }
        }
        scatteredLinkedList.removeIf(i -> i == 0);
    }

    @Benchmark
    public int calculate_sum_of_range() {
        return IntStream.range(0, N)
                        .map(i -> i * 3)
                        .map(i -> i * 7)
                        .sum();
    }

    @Benchmark
    public int calculate_sum_of_range_boxed() {
        return IntStream.range(0, N)
                        .boxed()
                        .map(i -> i * 3)
                        .map(i -> i * 7)
                        .reduce(0, Integer::sum);
    }

    @Benchmark
    public int calculate_sum_of_array_list() {
        return arrayList.stream()
                        .mapToInt(i -> i)
                        .map(i -> i * 5)
                        .sum();
    }

    @Benchmark
    public int calculate_sum_of_linked_list() {
        return linkedList.stream()
                         .mapToInt(i -> i)
                         .map(i -> i * 5)
                         .sum();
    }

    @Benchmark
    public int calculate_sum_of_linked_list_shuffled() {
        return shuffledLinkedList.stream()
                                 .mapToInt(i -> i)
                                 .map(i -> i * 5)
                                 .sum();
    }

    @Benchmark
    public int calculate_sum_of_linked_list_scattered() {
        return scatteredLinkedList.stream()
                                  .mapToInt(i -> i)
                                  .map(i -> i * 5)
                                  .sum();
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(PointerChasingBenchmarking.class.getName())
                .build();
        new Runner(opt).run();
    }
}
