# Parallel Streams in Java

> This is a tutorials course covering parallel streams in Java.

Tools used:

- JDK 11
- Maven
- JUnit 5, Mockito
- IntelliJ IDE

## Table of contents

1. Introduction to Parallel Streams
2. Performance Gains using Parallel Streams
3. Fork-Join Pool of Parallel Streams
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
parallel execution. Splitting the data source **_evenly_** is important to enabling correct parallel execution. An
unevenly split data source does more harm in parallel execution than its sequential counterpart.



---

## Chapter 02. Performance Gains using Parallel Streams

---

## Chapter 03. Fork-Join Pool of Parallel Streams

---

## Chapter 04. Parallel Collectors

---

## Chapter 05. Good practices using Parallel Streams

---

