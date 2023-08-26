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

### Chapter 01. Introduction to Parallel Streams

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

Parallel streams enable us to execute code in parallel on separate cores. The final result is the combination of each
individual outcome.

However, the order of execution is out of our control. It may change every time we run the program.

We might think that it's always faster to divide the work on more cores. But that is often not the case.

We need to use the parallel streams in **_right way_**, otherwise it will cause more harm than benefits.

We need to learn:

- how data is processes in parallel stream
- how the data is split and joined
- what can affect performance and detect bottlenecks
- how to choose our source of data

---

### Chapter 02. Performance Gains using Parallel Streams

---

### Chapter 03. Fork-Join Pool of Parallel Streams

---

### Chapter 04. Parallel Collectors

---

### Chapter 05. Good practices using Parallel Streams

---

