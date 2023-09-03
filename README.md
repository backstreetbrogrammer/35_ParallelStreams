# Parallel Streams in Java

> This is a tutorials course covering parallel streams in Java.

Tools used:

- JDK 11
- Maven
- JUnit 5, Mockito
- IntelliJ IDE

## Table of contents

1. [Introduction to Parallel Streams](https://github.com/backstreetbrogrammer/35_ParallelStreams#chapter-01-introduction-to-parallel-streams)
    - [Interview Problem 1 (Point72 Hedge Fund): How to count huge number of transactions in a trading day](https://github.com/backstreetbrogrammer/35_ParallelStreams#interview-problem-1-point72-hedge-fund-how-to-count-huge-number-of-transactions-in-a-trading-day)
    - [Difference in `parallelStream()` and `stream().parallel()`](https://github.com/backstreetbrogrammer/35_ParallelStreams#difference-in-parallelstream-and-streamparallel)
    - [Performance benchmarking using JMH](https://github.com/backstreetbrogrammer/35_ParallelStreams#performance-benchmarking-using-jmh)
2. [Performance Gains using Parallel Streams](https://github.com/backstreetbrogrammer/35_ParallelStreams#chapter-02-performance-gains-using-parallel-streams)
    - [Autoboxing](https://github.com/backstreetbrogrammer/35_ParallelStreams#autoboxing)
    - [Pointer chasing](https://github.com/backstreetbrogrammer/35_ParallelStreams#pointer-chasing)
3. [Fork-Join Pool of Parallel Streams](https://github.com/backstreetbrogrammer/35_ParallelStreams#chapter-03-fork-join-pool-of-parallel-streams)
4. Parallel Collectors
5. Good practices using Parallel Streams

---

## Chapter 01. Introduction to Parallel Streams

**Java 8** introduced the `Stream` API that makes it easy to iterate over collections as streams of data.

A `Stream` is simply a **wrapper** around a data source, allowing us to perform bulk operations on the data in a
convenient way.

It does **NOT** store data or make any changes to the underlying data source. Rather, it adds support for
**functional-style** operations on data pipelines.

By default, any stream operation is processed **sequentially**, unless explicitly specified as parallel.

Sequential streams use a **single thread** to process the pipeline.

**Example**

**Sequential Stream**

```
    final List<Student> students = List.of(...);
    double average = students.stream()
                       .mapToInt(Student::getAge)
                       .filter(age -> age > 20)
                       .average()
                       .orElseThrow(); // Java 11
```

It's also very easy to create streams that execute in **parallel** and make use of multiple processor cores.

Converting it into **parallel** stream is very simple.

**Java 8** introduced the `parallelStream()` method to the `Collection` interface and the `parallel()` method to the
`BaseStream` interface.

We can convert the stream to a parallel stream in two ways:

- `Collections.parallelStream()`
- `BaseStream.parallel()`

**Parallel Stream**

```
    final List<Student> students = List.of(...);
    double average = students.stream()
                       .mapToInt(Student::getAge)
                       .filter(age -> age > 20)
                       .parallel() // parallel
                       .average()
                       .orElseThrow();
```

**OR**

```
    final List<Student> students = List.of(...);
    double average = students.parallelStream() // parallel
                       .mapToInt(Student::getAge)
                       .filter(age -> age > 20)
                       .average()
                       .orElseThrow();
```

When a stream executes in **parallel**, the Java runtime splits the stream into multiple sub-streams.

There is a **special** bit which is `set` => any `Stream` **terminal** operation triggers all the **intermediate**
operations. JVM checks for this special bit and if it is set, then it executes all the operations in parallel.

Parallel streams enable us to execute code in parallel on separate cores. The final result is the combination of each
individual outcome.

However, the _**order**_ of execution is out of our control. It may change every time we run the program.

We might think that it's always faster to divide the work on more cores. But that is often not the case.

We need to use the parallel streams in **_right way_**, otherwise it will cause more harm than benefits.

We need to learn:

- how data is processed in parallel stream
- how the data is split and joined
- what can affect performance and how to detect bottlenecks
- how to choose our source of data

### Interview Problem 1 (Point72 Hedge Fund): How to count huge number of transactions in a trading day

In the day-to-day trading systems in hedge funds, millions of orders and trades are done on a trading day. Provide a
mechanism to efficiently count all the orders and trades done with **quantity** greater than `1000` (say) in a day.

**Solution**

Suppose the `Order` class looks like this:

```
public class Order {

    private int orderId;
    private String symbol;
    private double price;
    private int quantity;
    private String side;

    //constructor, getters and setters
}
```

If we just want to use default **sequential** streams:

```
    final long usingCollectionsSequential(final Collection<Order> orders, final int qty) {
        final AtomicLong bigOrders = new AtomicLong();
        orders.forEach(order -> {               // default is sequential stream
            if (order.getQuantity() >= qty) {
                bigOrders.getAndIncrement();
            }
        });
        return bigOrders.get();
    }
```

However, we can leverage parallel streams here and find the count more efficiently than doing it serially. The **order**
of execution in our example does not impact the final result in any way, making it a perfect candidate for parallel
Stream operation.

```
    final long usingCollectionsParallel(final Collection<Order> orders, final int qty) {
        final AtomicLong bigOrders = new AtomicLong();
        orders.parallelStream()             // using parallel stream
              .forEach(order -> {
                  if (order.getQuantity() >= qty) {
                      bigOrders.getAndIncrement();
                  }
              });
        return bigOrders.get();
    }
```

**Under the hood**

The default implementation of the `parallelStream()` method creates a parallel `Stream` from
the `Collection's` `Spliterator<T>` interface.

The `Spliterator` is an object for traversing and partitioning elements of its source. A `Spliterator` can partition off
some elements of its source using its `trySplit()` method to make it eligible for possible parallel operations.

The `Spliterator` API, similar to an `Iterator`, allows for the traversal of elements of its source and was designed to
support efficient **parallel** traversal.

The `Collection's` default `Spliterator` will be used in `parallelStream()` invocation.

**Using BaseStream.parallel()**

We can achieve the same result by first converting the collection to a `Stream`. We can convert the **sequential**
stream generated as a result into a **parallel** stream by calling `parallel()` on it. Once we have a parallel stream,
we can find our result in the same way we have done above.

```
    final long usingStreamParallel(final Collection<Order> orders, final int qty) {
        final AtomicLong bigOrders = new AtomicLong();
        orders.stream()
              .parallel()
              .forEach(order -> {
                  if (order.getQuantity() >= qty) {
                      bigOrders.getAndIncrement();
                  }
              });
        return bigOrders.get();
    }
```

The `BaseStream` interface will split the underlying data as much as the source collection's default `Spliterator` will
allow and then use the **_Fork-Join framework_** to convert it into a parallel Stream.

The result of both approaches bears the same result.

### Difference in `parallelStream()` and `stream().parallel()`

`Collections.parallelStream()` uses the source collection's default `Spliterator` to split the data source to enable
parallel execution. Splitting the data source **_evenly_** is important for enabling correct parallel execution. An
unevenly split data source does more harm in parallel execution than its sequential counterpart.

A developer can always override `Spliterator` interface and implement its `trySplit()` method incorrectly => not
splitting the data source **_evenly_**.

However, there is no way to override `stream().parallel()` implementation which always tries to return a parallel
version of the stream provided to it.

### Performance benchmarking using JMH

To check whether converting a sequential stream to parallel stream will improve performance or not, it is always
advisable to **measure** the performance.

JMH can be used to do **micro-benchmarking** => it means, we can measure performance of individual **methods** in a
class.

We should include maven dependencies: `jmh-core`, `jmh-generator-annprocess` and include the plugin:
`maven-shade-plugin` in `pom.xml`.

We will use the example method `probablePrime()` as it is a very heavy load CPU computation for all our JMH tests:

```
BigInteger.probablePrime(int bitLength, Random rnd)
// Returns a positive BigInteger that is probably prime, with the specified bitLength.
```

Code snippet:

```
    BigInteger probablePrime(int BIT_LENGTH) {
        return BigInteger.probablePrime(BIT_LENGTH,
                                        ThreadLocalRandom.current());
    }
```

`BIT_LENGTH` tunes the size of the prime number.

The `random` values generator provides **seeds** to generate the prime number.

Suppose we want to create list of probable prime numbers.

```
        final List<BigInteger> primes = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            primes.add(probablePrime(BIT_LENGTH));
        }
```

Doing the same using **sequential** streams:

```
        List<BigInteger> primes =
                IntStream.range(0, N)
                         .limit(N)
                         .mapToObj(i -> probablePrime(BIT_LENGTH))
                         .collect(Collectors.toList());
```

Using **parallel** streams:

```
        List<BigInteger> primes =
                IntStream.range(0, N)
                         .parallel()
                         .mapToObj(i -> probablePrime(BIT_LENGTH))
                         .collect(Collectors.toList());
```

Complete benchmark test class:

```java
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Warmup(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ProbablePrimeBenchmarking {
    @Param({"10", "100"})
    private int N;

    @Param({"64", "128"})
    private int BIT_LENGTH;

    BigInteger probablePrime() {
        return BigInteger.probablePrime(BIT_LENGTH,
                                        ThreadLocalRandom.current());
    }

    @Benchmark
    public List<BigInteger> sum_of_N_Primes() {
        final List<BigInteger> pps = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            final BigInteger pp = BigInteger.probablePrime(BIT_LENGTH,
                                                           ThreadLocalRandom.current());
            pps.add(pp);
        }
        return pps;
    }

    @Benchmark
    public List<BigInteger> sum_of_N_Primes_no_resize() {
        final List<BigInteger> pps = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            final BigInteger pp = BigInteger.probablePrime(BIT_LENGTH,
                                                           ThreadLocalRandom.current());
            pps.add(pp);
        }
        return pps;
    }

    @Benchmark
    public List<BigInteger> generate_N_primes() {
        return IntStream.range(0, N)
                        .mapToObj(i -> probablePrime())
                        .collect(toList());
    }

    @Benchmark
    public List<BigInteger> generate_N_primes_limit() {
        return Stream.generate(() -> probablePrime())
                     .limit(N)
                     .collect(toList());
    }

    @Benchmark
    public List<BigInteger> generate_N_primes_parallel() {
        return IntStream.range(0, N)
                        .parallel()
                        .mapToObj(i -> probablePrime())
                        .collect(toList());
    }

    @Benchmark
    public List<BigInteger> generate_N_primes_parallel_limit() {
        return Stream.generate(() -> probablePrime())
                     .parallel()
                     .limit(N)
                     .collect(toList());
    }

    @Benchmark
    public List<BigInteger> generate_N_primes_parallel_unordered() {
        return IntStream.range(0, N)
                        .unordered()
                        .parallel()
                        .mapToObj(i -> probablePrime())
                        .collect(toList());
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ProbablePrimeBenchmarking.class.getName())
                .build();
        new Runner(opt).run();
    }

}
```

To run the benchmark test:

- go to the terminal or command prompt
- run `mvn clean install` => this will create `target\benchmarks.jar`
- run the JMH tests => `java -jar target\benchmarks.jar`

**Output**

```
Benchmark                                                       (BIT_LENGTH)  (N)  Mode  Cnt   Score   Error  Units
ProbablePrimeBenchmarking.sum_of_N_Primes                                 64   10  avgt   15   2.674 ± 0.217  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes                                 64  100  avgt   15  29.664 ± 8.653  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes                                128   10  avgt   15   7.581 ± 0.406  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes                                128  100  avgt   15  66.992 ± 6.050  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes_no_resize                       64   10  avgt   15   2.402 ± 0.216  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes_no_resize                       64  100  avgt   15  27.921 ± 5.307  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes_no_resize                      128   10  avgt   15   7.382 ± 0.150  ms/op
ProbablePrimeBenchmarking.sum_of_N_Primes_no_resize                      128  100  avgt   15  75.847 ± 5.966  ms/op

ProbablePrimeBenchmarking.generate_N_primes                               64   10  avgt   15   2.249 ± 0.072  ms/op
ProbablePrimeBenchmarking.generate_N_primes                               64  100  avgt   15  22.502 ± 1.242  ms/op
ProbablePrimeBenchmarking.generate_N_primes                              128   10  avgt   15   7.740 ± 1.235  ms/op
ProbablePrimeBenchmarking.generate_N_primes                              128  100  avgt   15  71.972 ± 5.984  ms/op
ProbablePrimeBenchmarking.generate_N_primes_limit                         64   10  avgt   15   2.417 ± 0.270  ms/op
ProbablePrimeBenchmarking.generate_N_primes_limit                         64  100  avgt   15  24.296 ± 3.817  ms/op
ProbablePrimeBenchmarking.generate_N_primes_limit                        128   10  avgt   15   6.534 ± 0.337  ms/op
ProbablePrimeBenchmarking.generate_N_primes_limit                        128  100  avgt   15  78.284 ± 2.517  ms/op

ProbablePrimeBenchmarking.generate_N_primes_parallel                      64   10  avgt   15   1.371 ± 0.018  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel                      64  100  avgt   15  12.679 ± 1.257  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel                     128   10  avgt   15   3.555 ± 0.077  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel                     128  100  avgt   15  31.999 ± 0.480  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_limit                64   10  avgt   15   1.724 ± 0.032  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_limit                64  100  avgt   15  16.722 ± 0.301  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_limit               128   10  avgt   15   4.679 ± 0.495  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_limit               128  100  avgt   15  54.409 ± 9.498  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_unordered            64   10  avgt   15   1.601 ± 0.171  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_unordered            64  100  avgt   15  15.237 ± 1.560  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_unordered           128   10  avgt   15   3.884 ± 0.574  ms/op
ProbablePrimeBenchmarking.generate_N_primes_parallel_unordered           128  100  avgt   15  30.179 ± 0.278  ms/op
```

As evident from the output, parallel streams performance is much better than sequential streams and for loops.

---

## Chapter 02. Performance Gains using Parallel Streams

While using parallel streams, there are 2 features which can affect performance:

- Autoboxing
- Pointer chasing

### Autoboxing

**Boxing** refers to the conversion of a primitive value into an object of the corresponding wrapper class. For example,
converting `int` to `Integer` class.

**Unboxing** on the other hand refers to converting an object of a wrapper type to its corresponding primitive value.
For example, conversion of `Integer` to `int`.

Java can box and unbox automatically and this is called **Autoboxing**.

```
int i = 5;
Integer j = 3; // auto-boxing
int k = i + j; // auto-unboxing
Integer l = i + j; // auto-unboxing then auto-boxing
Integer m = j + l; // auto-unboxing then auto-boxing

int[] ints = {1, 2, 3}; // array of int
Integer[] integers = {1, 2, 3}; // array of Integer
List<Integer> list = List.of(1, 2, 3);
```

Autoboxing was added to provide support for **Collections framework** and **Generics**. However, it has a **cost**
associated for all the conversions.

Let's measure the performance using JMH.

```java
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
```

Build and install the project using `mvn clean install`

Run the benchmark test:
`java -jar target/benchmarks.jar AutoboxingBenchmarking.calculate_sum_of_ints AutoboxingBenchmarking.calculate_sum_of_integers`

**Output:**

```
Benchmark                                                                  (N)  Mode  Cnt  Score   Error  Units
ch02_performanceGains.AutoboxingBenchmarking.calculate_sum_of_integers  100000  avgt   15  0.337 ± 0.070  ms/op
ch02_performanceGains.AutoboxingBenchmarking.calculate_sum_of_ints      100000  avgt   15  0.037 ± 0.001  ms/op
```

Performance is very poor for wrapper `Integer` sum as compared to primitive `int` sum because of a lot of autoboxing.

### Pointer chasing

As we have seen multicore CPU architecture before where we have different level of caches present: L1, L2 and L3.

![CPUCaches](CPUCaches.PNG)

The memory is organized in cache-lines and data is transferred line by line between the main memory and the CPU caches.

An array is stored in a contiguous zone of the memory.

So, for an array of primitive **ints**, the data can be fetched very efficiently from memory to CPU caches.

However, for an array of wrapper **Integers**, only the **references** to different `Integer` objects (which are spread
across the heap) are stored.

Thus, to fetch the actual `Integer` objects pointed by these references is not cache-friendly and has got a cost. This
phenomenon is called **pointer chasing**.

Things are still better in `ArrayList<Integer>` as at-least the references are stored contiguously in memory lines.

![ArrayList](ArrayList.PNG)

In a `LinkedList<Integer>`, even the references are scattered over memory, and we need to trace all the nodes from head
to tail to fetch it.

![LinkedList](LinkedList.PNG)

Let's measure the performance of pointer chasing using JMH.

```java
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
```

Build and install the project using `mvn clean install`

Run the benchmark test:
`java -jar target/benchmarks.jar PointerChasingBenchmarking`

**Output:**

```
Benchmark                                                                                   (N)  Mode  Cnt   Score   Error  Units
ch02_performanceGains.PointerChasingBenchmarking.calculate_sum_of_array_list             100000  avgt   15   1.041 ± 0.590  ms/op
ch02_performanceGains.PointerChasingBenchmarking.calculate_sum_of_range_boxed            100000  avgt   15   1.675 ± 0.378  ms/op
ch02_performanceGains.PointerChasingBenchmarking.calculate_sum_of_range                  100000  avgt   15   2.024 ± 0.256  ms/op
ch02_performanceGains.PointerChasingBenchmarking.calculate_sum_of_linked_list            100000  avgt   15   0.990 ± 0.168  ms/op
ch02_performanceGains.PointerChasingBenchmarking.calculate_sum_of_linked_list_shuffled   100000  avgt   15   4.289 ± 2.091  ms/op
ch02_performanceGains.PointerChasingBenchmarking.calculate_sum_of_linked_list_scattered  100000  avgt   15  10.386 ± 0.741  ms/op
```

Performance wise, it's better to use `ArrayList` than `LinkedList`.

`LinkedList (scattered)` has the worst performance of all.

```
ArrayList > LinkedList > LinkedList (shuffled) > LinkedList (scattered)
```

---

## Chapter 03. Fork-Join Pool of Parallel Streams

Parallel streams are built on the Fork-Join Pool framework:

- a task is split in 2 sub-tasks
- sub-tasks are sent to a common pool of thread: **Fork-Join Pool**
- the results of each sub-tasks are joined
- and the global result is computed

![ForkJoin](ForkJoin.PNG)

The common **Fork-Join Pool**:

- is a pool of threads
- created when the JVM is created
- this pool is used for **all** the parallel streams running on a single JVM instance
- the **size** of the pool is **fixed** by the number of **virtual cores**

However, the API allows us to specify the number of threads it will use by passing a JVM parameter:

```
-D java.util.concurrent.ForkJoinPool.common.parallelism=4
```

It's important to remember that this is a global setting and that it will affect all parallel streams and any other
fork-join tasks that use the common pool. Thus, this parameter should not be modified unless we have a very good reason
for doing so.

### Interview Problem 2 (JP Morgan Chase): Identify the issue in the given code snippet

The fork-join framework is in charge of splitting the source data between worker threads and handling callback on task
completion.

Code snippet for calculating a sum of integers in parallel:

```java
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParallelStreamsIssues {

    @Test
    void testParallelSumOfIntegers() {
        final List<Integer> listOfNumbers = List.of(1, 2, 3, 4);
        final int sum = listOfNumbers.parallelStream()
                                     .reduce(5, Integer::sum);
        assertEquals(15, sum);
    }
}
```

**Output**

```
org.opentest4j.AssertionFailedError: 
Expected :15
Actual   :30
<Click to see difference>

	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:151)
	...
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:145)
	at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:528)
	at com.backstreetbrogrammer.ch03_forkJoin.ParallelStreamsIssues.testParallelSumOfIntegers(ParallelStreamsIssues.java:16)
	...
	...
```

Sum of first 5 integers 1...5 is `15`.

However, the above test always **fails**!

If we use **sequential** stream, `sum` is always correct as `15`.

But since the `reduce()` operation is handled in **parallel**, the number `5` actually gets added up in **every** worker
thread:

![ParallelSum](ParallelSum.PNG)

The actual result might differ depending on the number of threads used in the common fork-join pool.

For example, when we run this test case in computer with 4 virtual cores, then 5 is added to each split of data.

```
[1,2,3,4]

T1 = 5 + 1
T2 = 5 + 2
T3 = 5 + 3
T4 = 5 + 4

Total = 6 + 7 + 8 + 9 = 30
```

**Solution**

Number `5` should be added outside the parallel stream:

```
    @Test
    void testParallelSumOfIntegersCorrect() {
        final List<Integer> listOfNumbers = List.of(1, 2, 3, 4);
        final int sum = listOfNumbers.parallelStream()
                                     .reduce(0, Integer::sum) + 5;
        assertEquals(15, sum);
    }
```

Therefore, we need to be very careful about which operations can be run in parallel.

Besides, the default **common** thread pool, it's also possible to run a parallel stream in a **custom** thread pool.

```
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
```

---

## Chapter 04. Parallel Collectors

---

## Chapter 05. Good practices using Parallel Streams

---

