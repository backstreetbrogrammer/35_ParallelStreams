package com.backstreetbrogrammer.ch01_intro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelStreamIntroTest {

    private final int N = 10;
    private final int BIT_LENGTH = 64;

    @Test
    @DisplayName("Test probablePrime() using for loop")
    void testProbablePrimeUsingForLoop() {
        final List<BigInteger> primes = new ArrayList<>();
        final Instant start = Instant.now();
        for (int i = 0; i < N; i++) {
            primes.add(probablePrime(BIT_LENGTH));
        }
        final long timeElapsed = (Duration.between(start, Instant.now()).toMillis());
        System.out.printf("total time taken (for loop): %d ms%n%n", timeElapsed);
    }

    @Test
    @DisplayName("Test probablePrime() using sequential stream")
    void testProbablePrimeUsingSequentialStream() {
        final Instant start = Instant.now();
        final List<BigInteger> primes =
                IntStream.range(0, N)
                         .limit(N)
                         .mapToObj(i -> probablePrime(BIT_LENGTH))
                         .collect(Collectors.toList());
        final long timeElapsed = (Duration.between(start, Instant.now()).toMillis());
        System.out.printf("total time taken (sequential stream): %d ms%n%n", timeElapsed);
    }

    @Test
    @DisplayName("Test probablePrime() using parallel stream")
    void testProbablePrimeUsingParallelStream() {
        final Instant start = Instant.now();
        final List<BigInteger> primes =
                IntStream.range(0, N)
                         .parallel()
                         .mapToObj(i -> probablePrime(BIT_LENGTH))
                         .collect(Collectors.toList());
        final long timeElapsed = (Duration.between(start, Instant.now()).toMillis());
        System.out.printf("total time taken (parallel stream): %d ms%n%n", timeElapsed);
    }

    BigInteger probablePrime(final int BIT_LENGTH) {
        return BigInteger.probablePrime(BIT_LENGTH,
                                        ThreadLocalRandom.current());
    }


}
