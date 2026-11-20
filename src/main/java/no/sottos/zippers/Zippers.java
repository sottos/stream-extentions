package no.sottos.zippers;

import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static no.sottos.zippers.Functions.toArrayArgs;

///
/// # Combining several streams into one
/// This class contains a set of static functions which makes it easy to combine two or more streams
/// and handle the combined result in a stream pipeline
///
/// ## Functions for different places in the pipeline
/// There are three functions which merges streams, depending on where in the pipeline.
/// 1. Merging streams before the stream operations are started, the **Zippers.zip(...** function
/// 2. Merging streams using the intermediate *.gather(...)* operation, the **Zippers.zipWith(...** returning a gatherer.
/// 3. Merging streams in a collector (terminal operation), the function **Zippers.zipped(...** returning a collector.
///
///
/// ## Spliterator based zippers
/// - initial operation
/// - used at start of chain, producing the initial stream
/// - allows merging of infinite streams
/// - extracts one element from each stream and combines them into one type, then pushes that element downstream.
/// - combined stream is sequential
///
/// ex:
///
/// **zip(tStream, uStream, (t,u) -> combineTAndUFunc).map(r -> handle combined elem).....  etc.**
///
/// ### Termination criteria for spliterator based zippers
/// - The combined stream pipeline terminates when the shortest stream is terminated, this is the default.
/// - Default operation can be overridden using UNTIL_ALL_STREAMS_END, which terminates when all streams are empty
///
/// ## Gatherer based zippers
/// - intermediate operation
/// - used in the midst of a chain, merging one or more streams with the upstream into the pipeline
/// - allows merging of infinite streams, the result stream will have at most the length of the upstream stream
/// - gatherer is sequential
///
/// ex:
///
///  **tStream.map(...).gather(zipWith(uStream, (t,u) -> combineTAndUFunc)).map(r -> handle combined elem)..... etc.**
///
/// ### Termination criteria for gatherer based zippers
/// - The combined stream pipeline terminates when the shortest stream is terminated, this is the default.
/// - Default operation can be overridden using UNTIL_ALL_STREAMS_END, which terminates when all streams are empty
/// - The combined stream are always under control by the upstream, when empty upstream the combined stream is terminated
///
///
/// ## Collector based zippers
/// - terminating operation
/// - used to merge a stream of data into a collector
/// - allows merging of infinite streams, the result stream will have at most the length of the upstream stream
/// - collector is sequential
///
/// ex:
///
///  **stream.collect(zipped(Collectors.toList(), streamU, (t,u) -> combine(t,u)))**
/// - the list will be a list of results from the calls to combine
///
/// ### Termination criteria for collector based zippers
/// - The combined collector terminates when the shortest stream is terminated, this is the default.
/// - Default operation can be overridden using UNTIL_ALL_STREAMS_END, which terminates when all streams are empty
/// - The combined collector are always under control by the upstream, when empty upstream the combined collector is terminated
///
public class Zippers {

    /**
     * Controls when zipping stops relative to stream exhaustion.
     *
     * <p>When used with static import the values are self-explanatory at the call site:
     * <pre>
     *   zip(personStream, orderStream, UNTIL_ANY_STREAM_ENDS, (person, order) -> ...)
     *   zip(personStream, orderStream, UNTIL_ALL_STREAMS_END, (person, order) -> ...)
     * </pre>
     */
    public enum ZipUntil {
        /**
         * Stop as soon as the shortest stream is exhausted.
         * Null values are never passed to the combiner.
         * This is the default behaviour.
         */
        UNTIL_ONE_STREAM_ENDS,
        /**
         * Continue until all streams are exhausted.
         * Null is passed to the combiner for any stream that has been exhausted.
         */
        UNTIL_ALL_STREAMS_END
    }

    // Spliterator based zippers used at pipeline start, zip(tStream, uStream, (t,u) -> combined(t,u))...

    ///
    /// Builds a stream where elements from two streams are first combined into one before pushed downstream.
    /// Stops when the shortest stream is exhausted.
    ///
    /// @param tStream the stream of T's
    /// @param uStream the stream of U's
    /// @param combiner function combining the elements into one
    /// @return a stream with element type R
    /// @param <T> element type of first stream
    /// @param <U> element type of second stream
    /// @param <R> combined element type
    ///
    public static <T, U, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Functions.TwoArgs<T, U, R> combiner) {
        return zip(tStream, uStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    ///
    /// Builds a stream where elements from two streams are first combined into one before pushed downstream.
    ///
    /// @param tStream the stream of T's
    /// @param uStream the stream of U's
    /// @param until controls whether to stop at the shortest or longest stream
    /// @param combiner function combining the elements into one
    /// @return a stream with element type R
    /// @param <T> element type of first stream
    /// @param <U> element type of second stream
    /// @param <R> combined element type
    ///
    public static <T, U, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, ZipUntil until, Functions.TwoArgs<T, U, R> combiner) {
        return new MultiZipSpliterator.Zip2<>(tStream, uStream, combiner, until).stream();
    }

    public static <T, U, V, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Functions.ThreeArgs<T, U, V, R> combiner) {
        return zip(tStream, uStream, vStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, U, V, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, ZipUntil until, Functions.ThreeArgs<T, U, V, R> combiner) {
        return new MultiZipSpliterator.Zip3<>(tStream, uStream, vStream, combiner, until).stream();
    }

    public static <T, U, V, W, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Functions.FourArgs<T, U, V, W, R> combiner) {
        return zip(tStream, uStream, vStream, wStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, U, V, W, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, ZipUntil until, Functions.FourArgs<T, U, V, W, R> combiner) {
        return new MultiZipSpliterator.Zip4<>(tStream, uStream, vStream, wStream, combiner, until).stream();
    }

    public static <T, U, V, W, X, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, Functions.FiveArgs<T, U, V, W, X, R> combiner) {
        return zip(tStream, uStream, vStream, wStream, xStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, U, V, W, X, R> Stream<R> zip(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, ZipUntil until, Functions.FiveArgs<T, U, V, W, X, R> combiner) {
        return new MultiZipSpliterator.Zip5<>(tStream, uStream, vStream, wStream, xStream, combiner, until).stream();
    }

    public static <R> Stream<R> zipX(Functions.ArgsInArrayFunction<R> combiner, Stream<?>... streams) {
        return new MultiZipSpliterator.ZipX<>(streams, combiner, ZipUntil.UNTIL_ONE_STREAM_ENDS).stream();
    }

    public static <R> Stream<R> zipX(ZipUntil until, Functions.ArgsInArrayFunction<R> combiner, Stream<?>... streams) {
        return new MultiZipSpliterator.ZipX<>(streams, combiner, until).stream();
    }

    // Gatherer based zippers, used in the midst of a chain, tStream.gather(zipWith(uStream, (t,u) -> combine(t,u))).....

    ///
    /// Builds a gatherer where elements from upstream and one other stream are merged before pushed downstream.
    /// Stops when the shortest stream is exhausted.
    ///
    /// - The gatherer is ofSequential type.
    /// - When the upstream is empty merging is stopped
    ///
    /// @param uStream the stream of U's
    /// @param combiner function combining the elements into one
    /// @return a gatherer with element type R
    /// @param <T> element type of upstream
    /// @param <U> element type of stream to merge into pipeline
    /// @param <R> combined element type
    ///
    public static <T, U, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Functions.TwoArgs<T, U, R> combiner) {
        return zipWith(uStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    ///
    /// Builds a gatherer where elements from upstream and one other stream are merged before pushed downstream.
    ///
    /// - The gatherer is ofSequential type.
    /// - When the upstream is empty merging is stopped
    ///
    /// @param uStream the stream of U's
    /// @param until controls whether to stop at the shortest or longest stream
    /// @param combiner function combining the elements into one
    /// @return a gatherer with element type R
    /// @param <T> element type of upstream
    /// @param <U> element type of stream to merge into pipeline
    /// @param <R> combined element type
    ///
    public static <T, U, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, ZipUntil until, Functions.TwoArgs<T, U, R> combiner) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream}, toArrayArgs(combiner), until));
    }

    public static <T, U, V, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Stream<V> vStream, Functions.ThreeArgs<T, U, V, R> combiner) {
        return zipWith(uStream, vStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, U, V, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Stream<V> vStream, ZipUntil until, Functions.ThreeArgs<T, U, V, R> combiner) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream}, toArrayArgs(combiner), until));
    }

    public static <T, U, V, W, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Functions.FourArgs<T, U, V, W, R> combiner) {
        return zipWith(uStream, vStream, wStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, U, V, W, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, ZipUntil until, Functions.FourArgs<T, U, V, W, R> combiner) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream, wStream}, toArrayArgs(combiner), until));
    }

    public static <T, U, V, W, X, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, Functions.FiveArgs<T, U, V, W, X, R> combiner) {
        return zipWith(uStream, vStream, wStream, xStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, U, V, W, X, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, ZipUntil until, Functions.FiveArgs<T, U, V, W, X, R> combiner) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream, wStream, xStream}, toArrayArgs(combiner), until));
    }

    private static <T, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipGatherer(Supplier<MultiZipGatherBody<T, R>> multiZipGatherBodySupplier) {
        return Gatherer.ofSequential(
                multiZipGatherBodySupplier,
                MultiZipGatherBody::integrate,
                MultiZipGatherBody::finish
        );
    }

    // Collector based zippers, stream.collect(zipped(Collectors.toList(), streamU, (t,u) -> combine(t,u)))

    ///
    /// Builds a Collector which combines input from upstream and uStream and feeds a given collector with the combined input.
    /// - When the upstream is empty merging or combining is stopped
    /// Stops when the shortest stream is exhausted.
    ///
    /// @param collectorToExtend collector which will be called with elements of Combined
    /// @param uStream the stream to combine elements with
    /// @param combiner function combining the elements into one
    /// @return a collector with the same function as collectorToExtend, but which will be fed with Combined elements
    /// @param <T> upstream element type
    /// @param <Combined> type of combining upstream and uStream elements
    /// @param <R> the result type of the reduction operation in collectorToExtend
    /// @param <Accumulator> accumulator type of the collectorToExtend
    /// @param <C> type of collectorToExtend using Combined input
    /// @param <U> type of elements in uStream
    ///
    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Functions.TwoArgs<T, U, Combined> combiner) {
        return zipped(collectorToExtend, uStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    ///
    /// Builds a Collector which combines input from upstream and uStream and feeds a given collector with the combined input.
    /// - When the upstream is empty merging or combining is stopped
    ///
    /// @param collectorToExtend collector which will be called with elements of Combined
    /// @param uStream the stream to combine elements with
    /// @param until controls whether to stop at the shortest or longest stream
    /// @param combiner function combining the elements into one
    /// @return a collector with the same function as collectorToExtend, but which will be fed with Combined elements
    /// @param <T> upstream element type
    /// @param <Combined> type of combining upstream and uStream elements
    /// @param <R> the result type of the reduction operation in collectorToExtend
    /// @param <Accumulator> accumulator type of the collectorToExtend
    /// @param <C> type of collectorToExtend using Combined input
    /// @param <U> type of elements in uStream
    ///
    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, ZipUntil until, Functions.TwoArgs<T, U, Combined> combiner) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream},
                toArrayArgs(combiner),
                until
        );
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Stream<V> vStream, Functions.ThreeArgs<T, U, V, Combined> combiner) {
        return zipped(collectorToExtend, uStream, vStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Stream<V> vStream, ZipUntil until, Functions.ThreeArgs<T, U, V, Combined> combiner) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream},
                toArrayArgs(combiner),
                until
        );
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Functions.FourArgs<T, U, V, W, Combined> combiner) {
        return zipped(collectorToExtend, uStream, vStream, wStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, ZipUntil until, Functions.FourArgs<T, U, V, W, Combined> combiner) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream, wStream},
                toArrayArgs(combiner),
                until
        );
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W, X>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, Functions.FiveArgs<T, U, V, W, X, Combined> combiner) {
        return zipped(collectorToExtend, uStream, vStream, wStream, xStream, ZipUntil.UNTIL_ONE_STREAM_ENDS, combiner);
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W, X>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, ZipUntil until, Functions.FiveArgs<T, U, V, W, X, Combined> combiner) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream, wStream, xStream},
                toArrayArgs(combiner),
                until
        );
    }
}