package com.backstreetbrogrammer.ch03_forkJoin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

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
}
