package org.example.mains;

import org.example.general.Tup2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public class ZipWithIndexUsingCollector {
    public static void main(String[] args) {
        System.out.println("Using collector");
        stringWithIndexBounded();
        counting_test();
/*
        stringWithIndexUnBounded(6);
        stringWithDoubleIndexBounded();
        stringWithDoubleIndexUnBounded(2);
        stringWithDoubleIndexUnBounded(5);
*/
    }

    private static void stringWithIndexBounded() {
        System.out.println("Join two stream to a 2-tuple stream, terminate when shortest stream terminates");

        List<Tup2<String, Integer>> collect = Stream
                .of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipWith(Stream.iterate(1, x -> x + 1), Collectors.toList()));
        System.out.println("collected = " + collect);
    }
    private static void counting_test() {
        System.out.println("Join two stream to a 2-tuple stream, terminate when shortest stream terminates");

        var collect = Stream
                .of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipWith(Stream.iterate(1, x -> x + 1), Collectors.counting()));
        System.out.println("collected = " + collect);
    }

    record State<U, X>(Iterator<U> uIter, X x) {
    }

    private static <T, U, R, X, C extends Collector<Tup2<T, U>, X, R>>
    Collector<T, State<U, X>, R> zipWith(Stream<U> uStream, C collectorToExtend) {
        return new Collector<>() {

            final Set<Characteristics> characteristics;

            {
                var copy = new HashSet<>(collectorToExtend.characteristics());
                copy.remove(CONCURRENT);
                copy.remove(IDENTITY_FINISH);
                characteristics = copy;
            }

            @Override
            public Supplier<State<U, X>> supplier() {
                return () ->
                        new State<>(uStream.iterator(), collectorToExtend.supplier().get());
            }

            @Override
            public BiConsumer<State<U, X>, T> accumulator() {
                BiConsumer<X, Tup2<T, U>> innerAccumulator = collectorToExtend.accumulator();
                return (state, t) -> innerAccumulator.accept(state.x, createTup(state, t));
            }


            @Override
            public BinaryOperator<State<U, X>> combiner() {
                return (s1, s2) -> {
                    throw new IllegalStateException("This is not a concurrent stream collector");
                };
            }

            @Override
            public Function<State<U, X>, R> finisher() {
                Function<X, R> innerFinisher = collectorToExtend.finisher();
                return s -> innerFinisher == null ? (R)s.x() : innerFinisher.apply(s.x());
            }

            @Override
            public Set<Characteristics> characteristics() {
                return characteristics;
            }

            private static <T, U, X> Tup2<T, U> createTup(State<U, X> state, T t) {
                final Tup2<T, U> tup2;
                if (state.uIter().hasNext()) {
                    tup2 = new Tup2<>(t, state.uIter().next());
                } else {
                    tup2 = new Tup2<>(t, null);
                }
                return tup2;
            }
        };
    }
/*

    private static void stringWithIndexUnBounded(int n) {
        System.out.println("Join two streams to a 2-tuple stream, terminate after " + n + " stream extractions");

        Stream
                .iterate(1, x -> x + 1)
                .gather(zipWith(Stream.of("Per", "Pål", "Espen", "Prinsesse"), WHEN_AT_LEAST_ONE_HAVE_DATA))
                .limit(n)
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexBounded() {
        System.out.println("Join three streams to a 3-tuple stream, terminate when shortest stream is empty");

        Stream
                .iterate(1, x -> x + 1)
                .gather(zipWith(Stream.of("Per", "Pål", "Espen", "Prinsesse")))
                .gather(zipWith(Stream.of(11, 12, 13)))
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexUnBounded(int n) {
        System.out.println("Join three streams to a 3-tuple stream, terminate after " + n + " stream extractions");

        Stream
                .of(11, 12, 13)
                .gather(zipWith(Stream.of("Per", "Pål", "Espen", "Prinsesse"), WHEN_AT_LEAST_ONE_HAVE_DATA))
                .gather(zipWith(Stream.iterate(1, x -> x + 1), WHEN_AT_LEAST_ONE_HAVE_DATA))
                .limit(n)
                .forEach(System.out::println);
    }
*/
}
