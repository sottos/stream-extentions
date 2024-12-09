package org.example.simple;

import org.example.general.Tup2;

import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static org.example.simple.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

public final class Zippers {
    private Zippers() {}

    public static <T, U> Gatherer<T, ?, Tup2<T, U>> zipWith(Stream<U> uStream) {
        return zipWith(uStream, WHEN_ALL_HAVE_DATA);
    }

    public static <T, U> Gatherer<T, ?, Tup2<T, U>> zipWith(Stream<U> uStream, ZipWhen zipWhen) {
        return Gatherer.ofSequential(
                () -> new ZipGatherer(uStream, zipWhen),
                ZipGatherer::integrate,
                ZipGatherer::finish
        );
    }

    public static <T, U> Zip2<T,U> zip2(Stream<T> tStream, Stream<U> uStream) {
        return zip2(tStream, uStream, WHEN_ALL_HAVE_DATA);
    }
    public static <T, U> Zip2<T,U> zip2(Stream<T> tStream, Stream<U> uStream, ZipWhen zipWhen) {
        return new Zip2<>(tStream, uStream);
    }
    public static <T, U, V> Zip3<T,U,V> zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> v) {
        return zip3(tStream, uStream, v, WHEN_ALL_HAVE_DATA);
    }
    public static <T, U, V> Zip3<T,U, V> zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, ZipWhen zipWhen) {
        return new Zip3<>(tStream, uStream, vStream, zipWhen);
    }

    public enum ZipWhen {
        /**
         * The zipping will proceed only when all underlying streams can advance
         */
        WHEN_ALL_HAVE_DATA,
        /**
         * The zipping will proceed when at least one underlying stream can advance.
         * When one stream is exhausted, emit null element for that stream until the others are exhausted
         */
        WHEN_AT_LEAST_ONE_HAVE_DATA
    }
}
