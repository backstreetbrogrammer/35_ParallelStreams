package com.backstreetbrogrammer.ch03_forkJoin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParallelStreamsIssues {

    @Test
    void testParallelSumOfIntegers() {
        final List<Integer> listOfNumbers = List.of(1, 2, 3, 4);
        final int sum = listOfNumbers.parallelStream()
                                     .reduce(5, Integer::sum);
        //assertEquals(15, sum);
        assertEquals(30, sum);
    }

    @Test
    void testParallelSumOfIntegersCorrect() {
        final List<Integer> listOfNumbers = List.of(1, 2, 3, 4);
        final int sum = listOfNumbers.parallelStream()
                                     .reduce(0, Integer::sum) + 5;
        assertEquals(15, sum);
    }

    @Test
    void testCustomThreadPool() throws ExecutionException, InterruptedException {
        final List<Integer> listOfNumbers = List.of(1, 2, 3, 4, 5);
        final ForkJoinPool customThreadPool = new ForkJoinPool(4);
        final int sum = customThreadPool
                .submit(
                        () -> listOfNumbers.parallelStream()
                                           .reduce(0, Integer::sum))
                .get();
        customThreadPool.shutdown();
        assertEquals(15, sum);
    }

    @Test
    void testAssociativityIssues() {
        final int sum = IntStream.range(0, 10)
                                 .sum();
        System.out.printf("sum = %d%n", sum);

        final int sumParallel = IntStream.range(0, 10)
                                         .parallel()
                                         .sum();
        System.out.printf("parallel sum = %d%n", sumParallel);

        final int sumOfSquares = IntStream.range(0, 10)
                                          .reduce(0, (i1, i2) -> i1 * i1 + i2 * i2);
        System.out.printf("sum of squares = %d%n", sumOfSquares);

        final int sumOfSquaresParallel = IntStream.range(0, 10)
                                                  .map(i -> i * i)
                                                  .parallel()
                                                  .reduce(0, (i1, i2) -> i1 * i1 + i2 * i2);
        System.out.printf("parallel sum of squares = %d%n", sumOfSquaresParallel);
    }

    @Test
    @DisplayName("Display thread names in parallel stream")
    void displayThreadNamesInParallelStream() {
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();

        final int sum = IntStream.range(0, 1_000_000)
                                 .map(i -> i * 3)
                                 .parallel()
                                 .peek(i -> threadNames.add(Thread.currentThread().getName()))
                                 .sum();

        threadNames.forEach(System.out::println);
    }

    @Test
    @DisplayName("Execute a parallel stream in a custom Fork-Join Pool")
    void executeParallelStreamInCustomForkJoinPool() throws ExecutionException, InterruptedException {
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();
        final ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        final Callable<Integer> task = () -> IntStream.range(0, 1_000_000)
                                                      .map(i -> i * 3)
                                                      .parallel()
                                                      .peek(i -> threadNames.add(Thread.currentThread().getName()))
                                                      .sum();

        final ForkJoinTask<Integer> submit = forkJoinPool.submit(task);
        submit.get(); // blocking

        threadNames.forEach(System.out::println);
        forkJoinPool.shutdown();
    }

    @Test
    @DisplayName("Count the number of tasks each thread executed in the custom Fork-Join Pool")
    void countNumberOfTasksExecutedByEachThreadCustomForkJoinPool() throws ExecutionException, InterruptedException {
        final Map<String, Long> threadMap = new ConcurrentHashMap<>();
        final ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        final Callable<Integer> task = () -> IntStream.range(0, 1_000_000)
                                                      .map(i -> i * 3)
                                                      .parallel()
                                                      .peek(i -> threadMap.merge(Thread.currentThread().getName(),
                                                                                 1L, Long::sum))
                                                      .sum();
        final ForkJoinTask<Integer> submit = forkJoinPool.submit(task);
        submit.get(); // blocking

        threadMap.forEach((threadName, count) -> System.out.printf("Thread name: %s, Count of tasks: %d%n",
                                                                   threadName, count));
        forkJoinPool.shutdown();
    }

}
