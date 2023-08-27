package com.backstreetbrogrammer.ch01_intro;

import com.backstreetbrogrammer.model.Order;
import com.backstreetbrogrammer.model.Student;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ParallelStreamIntro {

    public static void main(final String[] args) {
        final var john = new Student("John", 18);
        final var mary = new Student("Mary", 16);
        final var thomas = new Student("Thomas", 21);
        final var rahul = new Student("Rahul", 23);
        final var jenny = new Student("Jenny", 17);
        final var tatiana = new Student("Tatiana", 25);

        final List<Student> students = List.of(john, mary, thomas, rahul, jenny, tatiana);

        final double averageSequential = students.stream()
                                                 .mapToInt(Student::getAge)
                                                 .filter(age -> age > 20)
                                                 .average()
                                                 .orElseThrow();

        System.out.println(averageSequential);

        final double averageParallel = students.parallelStream()
                                               .mapToInt(Student::getAge)
                                               .filter(age -> age > 20)
                                               .average()
                                               .orElseThrow();

        System.out.println(averageParallel);
    }

    final long usingCollectionsSequential(final Collection<Order> orders, final int qty) {
        final AtomicLong bigOrders = new AtomicLong();
        orders.forEach(order -> {
            if (order.getQuantity() >= qty) {
                bigOrders.getAndIncrement();
            }
        });
        return bigOrders.get();
    }

    final long usingCollectionsParallel(final Collection<Order> orders, final int qty) {
        final AtomicLong bigOrders = new AtomicLong();
        orders.parallelStream()
              .forEach(order -> {
                  if (order.getQuantity() >= qty) {
                      bigOrders.getAndIncrement();
                  }
              });
        return bigOrders.get();
    }

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
}
