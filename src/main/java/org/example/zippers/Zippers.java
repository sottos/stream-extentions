package org.example.zippers;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.zippers.Functions.LambdaWrappers.toArrayArgs;

public class Zippers {
    public static abstract class Zip<R> {
        MultiZipSpliterator<R> zipper;

        private Zip(Stream<?>[] streams, Functions.ArgsInArrayFunction<R> tupCreator, ZipWhen zipWhen) {
            zipper = new MultiZipSpliterator<>
                    (Arrays.stream(streams).map(s -> (Spliterator<?>) s.spliterator()).toArray(Spliterator<?>[]::new),
                            tupCreator,
                            zipWhen);
        }

        public Stream<R> stream() {
            return StreamSupport.stream(zipper, false);
        }
    }

    private static class Zip2<T, U, R> extends Zip<R> {
        private Zip2(Stream<T> tStream, Stream<U> uStream, Functions.TwoArgs<T, U, R> tupCreator, ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream},
                    toArrayArgs(tupCreator),
                    zipWhen);
        }
    }

    private static class Zip3<T, U, V, R> extends Zip<R> {
        private Zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Functions.ThreeArgs<T, U, V, R> tupCreator, ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream, vStream},
                    toArrayArgs(tupCreator),
                    zipWhen);
        }
    }

    private static class Zip4<T, U, V, W, R> extends Zip<R> {
        private Zip4(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Functions.FourArgs<T, U, V, W, R> tupCreator, ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream, vStream, wStream},
                    toArrayArgs(tupCreator),
                    zipWhen);
        }
    }

    private static class Zip5<T, U, V, W, X, R> extends Zip<R> {
        private Zip5(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, Functions.FiveArgs<T, U, V, W, X, R> tupCreator, ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream, vStream, wStream, xStream},
                    toArrayArgs(tupCreator),
                    zipWhen);
        }
    }

    private static class ZipX<R> extends Zip<R> {
        private ZipX(Stream<?>[] streams, Functions.ArgsInArrayFunction<R> tupCreator, ZipWhen zipWhen) {
            super(streams, tupCreator, zipWhen);
        }
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

    public static <T, U, R> Zip<R> zip(Functions.TwoArgs<T, U, R> tupCreator, Stream<T> tStream, Stream<U> uStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream);
    }

    public static <T, U, R> Zip<R> zip(Functions.TwoArgs<T, U, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream) {
        return new Zip2<>(tStream, uStream, tupCreator, zipWhen);
    }

    public static <T, U, V, R> Zip<R> zip(Functions.ThreeArgs<T, U, V, R> tupCreator, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream);
    }

    public static <T, U, V, R> Zip<R> zip(Functions.ThreeArgs<T, U, V, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        return new Zip3<>(tStream, uStream, vStream, tupCreator, zipWhen);
    }

    public static <T, U, V, W, R> Zip<R> zip(Functions.FourArgs<T, U, V, W, R> tupCreator, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream, wStream);
    }

    public static <T, U, V, W, R> Zip<R> zip(Functions.FourArgs<T, U, V, W, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return new Zip4<>(tStream, uStream, vStream, wStream, tupCreator, zipWhen);
    }

    public static <T, U, V, W, X, R> Zip<R> zip(Functions.FiveArgs<T, U, V, W, X, R> tupCreator, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream, wStream, xStream);
    }

    public static <T, U, V, W, X, R> Zip<R> zip(Functions.FiveArgs<T, U, V, W, X, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return new Zip5<>(tStream, uStream, vStream, wStream, xStream, tupCreator, zipWhen);
    }

    public static <R> Zip<R> zipX(Functions.ArgsInArrayFunction<R> tupCreator, Stream<?>... streams) {
        return new ZipX<R>(streams, tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA);
    }

    public static <R> Zip<R> zipX(Functions.ArgsInArrayFunction<R> tupCreator, ZipWhen zipWhen, Stream<?>... streams) {
        return new ZipX<R>(streams, tupCreator, zipWhen);
    }
}
