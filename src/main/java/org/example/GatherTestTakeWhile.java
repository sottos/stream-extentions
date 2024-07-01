package org.example;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static org.example.GatherTestTakeWhile.Tup2.Tup2;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatherTestTakeWhile {

    public static void main(String[] args) {
        AtomicInteger countAfterCheck = new AtomicInteger();
        AtomicInteger countBeforeCheck = new AtomicInteger();

        Stream.of("1", "1", "2")
                .gather(counterGatherer(countBeforeCheck))
                .takeWhile(s -> s.equals("1"))
                .gather(counterGatherer(countAfterCheck))
                .findFirst();

        assertEquals(countBeforeCheck.get(), countAfterCheck.get(),
                "should be equal");
        assertEquals(1, countAfterCheck.get(),
                "should be one");
        {
            List<Tup2<String, String>> list = Stream.of("abc", "123", "zxy")
                    .gather(zip(Stream.of("first", "second", "third")))
                    .toList();
            System.out.println("list = " + list);
        }
        {
            List<Tup2<String, String>> list = Stream.of("abc", "123", "zxy")
                    .gather(zip(Stream.of("first", "second")))
                    .toList();
            System.out.println("list = " + list);
        }
        {
            List<Tup2<String, String>> list = Stream.of("abc", "123", "zxy")
                    .gather(zip(Stream.of("first", "second","third","fourth")))
                    .toList();
            System.out.println("list = " + list);
        }
        {
            List<Tup2<String, String>> list = Stream.of("abc", "123", "zxy")
                    .gather(zip(Stream.of("first", "second","third","fourth")))
                    .filter(testUnzipped(s -> s.length() > 0 && Character.isAlphabetic(s.codePointAt(0))))
                    .toList();
            System.out.println("list = " + list);
        }
        {
            Stream<Tup2<String, String>> pipeline = Stream.of("abc", "123", "zxy")
                    .gather(zip(Stream.of("first", "second", "third", "fourth")));

            Optional<String> first = pipeline.map(unzipped(s -> s.toLowerCase())).findFirst();
            System.out.println("first = " + first);
        }
    }


    /**
     * Gatherer counting number of elements passed through it.
     * Integrator passes element to downstream and increments given AtomicInteger for each.
     */
    public static <T> Gatherer<T, ?, T> counterGatherer(AtomicInteger passthroughCounter) {
        return Gatherer.of((_, element, downstream) -> {
            passthroughCounter.incrementAndGet();
            return downstream.push(element);
        });
    }

    public static <T, U> Gatherer<T, Iterator<U>, Tup2<T, U>> zip(Stream<U> other) {
        return Gatherer.ofSequential(
                other::iterator,
                (iter, element, downstream) -> {
                    if (! iter.hasNext()) {
                        return false;
                    }
                    return downstream.push(Tup2(element, iter.next()));
                });
    }

    public record Tup2<T, U>(T a, U b) {
        @SuppressWarnings("MethodNameSameAsClassName")
        public static <T,U> Tup2<T,U> Tup2(T t, U u) { return new Tup2<>(t,u);}
    }

    public static <T, U, R> Function<Tup2<T, U>, R> zipped(BiFunction<? super T, ? super U, ? extends R> biFunction) {
        return tup2 -> biFunction.apply(tup2.a, tup2.b);
    }
    public static <T, U, R> Function<Tup2<T, U>,R> unzipped(Function<? super T, ? extends R> function) {
        return tup2 -> function.apply(tup2.a);
    }
    public static <T, U> Predicate<Tup2<T, U>> testUnzipped(Predicate<? super T> predicate) {
        return tup2 -> predicate.test(tup2.a);
    }
    public static <T, U> Predicate<Tup2<T, U>> testZipped(BiPredicate<? super T,? super U> predicate) {
        return tup2 -> predicate.test(tup2.a, tup2.b);
    }
    public static <T, U, R> Function<Tup2<T, U>, Tup2<R, U>> passZipped(Function<? super T, ? extends R> function) {
        return tup2 -> new Tup2<R, U>(function.apply(tup2.a), tup2.b);
    }

    public static <T, U, R> Function<Tup2<T, U>, Tup2<R, U>> withZippedPassZipped(BiFunction<? super T, ? super U, ? extends R> biFunction) {
        return tup2 -> new Tup2<R, U>(biFunction.apply(tup2.a, tup2.b), tup2.b);
    }
}

