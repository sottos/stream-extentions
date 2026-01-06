package no.sottos.zippers;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Gatherer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static no.sottos.zippers.Functions.LambdaWrappers.toArrayArgs;

public class Zippers {

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

    // GathererZippers, used in the midst of a chain map(...).zipWith((t,u) -> new Pair(t,u), uStream).....

    public static <T, U, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.TwoArgs<T, U, R> tupCreator, Stream<U> uStream) {
        return zipWith(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream);
    }

    public static <T, U, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.TwoArgs<T, U, R> tupCreator, ZipWhen zipWhen, Stream<U> uStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream}, toArrayArgs(tupCreator), zipWhen));
    }

    public static <T, U, V, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.ThreeArgs<T, U, V, R> tupCreator, Stream<U> uStream, Stream<V> vStream) {
        return zipWith(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream);
    }

    public static <T, U, V, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.ThreeArgs<T, U, V, R> tupCreator, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream}, toArrayArgs(tupCreator), zipWhen));
    }

    public static <T, U, V, W, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FourArgs<T, U, V, W, R> tupCreator, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zipWith(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream);
    }

    public static <T, U, V, W, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FourArgs<T, U, V, W, R> tupCreator, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream, wStream}, toArrayArgs(tupCreator), zipWhen));
    }

    public static <T, U, V, W, X, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FiveArgs<T, U, V, W, X, R> tupCreator, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zipWith(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream, xStream);
    }

    public static <T, U, V, W, X, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FiveArgs<T, U, V, W, X, R> tupCreator, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream, wStream, xStream}, toArrayArgs(tupCreator), zipWhen));
    }

    private static <T, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipGatherer(Supplier<MultiZipGatherBody<T, R>> multiZipGatherBodySupplier) {
        return Gatherer.ofSequential(
                multiZipGatherBodySupplier,
                MultiZipGatherBody::integrate,
                MultiZipGatherBody::finish
        );
    }

    // Spliterator zippers, used at start of chain: zip((t,u) -> new Pair<>(t,u), streamT, streamU).stream()....

    public static <T, U, R> Stream<R> zip(Functions.TwoArgs<T, U, R> tupCreator, Stream<T> tStream, Stream<U> uStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream);
    }

    public static <T, U, R> Stream<R> zip(Functions.TwoArgs<T, U, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream) {
        return new Zip2<>(tStream, uStream, tupCreator, zipWhen).stream();
    }

    public static <T, U, V, R> Stream<R> zip(Functions.ThreeArgs<T, U, V, R> tupCreator, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream);
    }

    public static <T, U, V, R> Stream<R> zip(Functions.ThreeArgs<T, U, V, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        return new Zip3<>(tStream, uStream, vStream, tupCreator, zipWhen).stream();
    }

    public static <T, U, V, W, R> Stream<R> zip(Functions.FourArgs<T, U, V, W, R> tupCreator, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream, wStream);
    }

    public static <T, U, V, W, R> Stream<R> zip(Functions.FourArgs<T, U, V, W, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return new Zip4<>(tStream, uStream, vStream, wStream, tupCreator, zipWhen).stream();
    }

    public static <T, U, V, W, X, R> Stream<R> zip(Functions.FiveArgs<T, U, V, W, X, R> tupCreator, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zip(tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream, wStream, xStream);
    }

    public static <T, U, V, W, X, R> Stream<R> zip(Functions.FiveArgs<T, U, V, W, X, R> tupCreator, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return new Zip5<>(tStream, uStream, vStream, wStream, xStream, tupCreator, zipWhen).stream();
    }

    public static <R> Stream<R> zipX(Functions.ArgsInArrayFunction<R> tupCreator, Stream<?>... streams) {
        return new ZipX<R>(streams, tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA).stream();
    }

    public static <R> Stream<R> zipX(Functions.ArgsInArrayFunction<R> tupCreator, ZipWhen zipWhen, Stream<?>... streams) {
        return new ZipX<R>(streams, tupCreator, zipWhen).stream();
    }

    // Collector zippers, map(...).collect(zipper((t,u) -> new Pair<>(t,u), streamU))

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.TwoArgs<T, U, JoinedInput> tupCreator, Stream<U> uStream) {
        return zipped(collectorToExtend, tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream);
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.TwoArgs<T, U, JoinedInput> tupCreator, ZipWhen zipWhen, Stream<U> uStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream},
                toArrayArgs(tupCreator),
                zipWhen
        );
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U, V>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.ThreeArgs<T, U, V, JoinedInput> tupCreator, Stream<U> uStream, Stream<V> vStream) {
        return zipped(collectorToExtend, tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream);
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U, V>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.ThreeArgs<T, U, V, JoinedInput> tupCreator, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream},
                toArrayArgs(tupCreator),
                zipWhen
        );
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U, V, W>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FourArgs<T, U, V, W, JoinedInput> tupCreator, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zipped(collectorToExtend, tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream);
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U, V, W>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FourArgs<T, U, V, W, JoinedInput> tupCreator, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream, wStream},
                toArrayArgs(tupCreator),
                zipWhen
        );
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U, V, W, X>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FiveArgs<T, U, V, W, X, JoinedInput> tupCreator, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zipped(collectorToExtend, tupCreator, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream, xStream);
    }

    public static <T, JoinedInput, R, Accumulator, C extends Collector<JoinedInput, Accumulator, R>, U, V, W, X>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FiveArgs<T, U, V, W, X, JoinedInput> tupCreator, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream, wStream, xStream},
                toArrayArgs(tupCreator),
                zipWhen
        );
    }

    // Spliterator Zippers helper classes
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


}
