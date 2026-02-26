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
/// **zip((t,u) -> combineTAndUFunc, tStream, uStream).map(r -> handle combined elem).....  etc.**
///
/// ### Termination criteria for spliterator based zippers
/// - The combined stream pipeline terminates when the shortest stream is terminated, this is the default.
/// - Default operation can be overridden using WHEN_AT_LEAST_ONE_HAVE_DATA, which terminates when all streams are empty
/// 
/// ## Gatherer based zippers
/// - intermediate operation
/// - used in the midst of a chain, merging one or more streams with the upstream into the pipeline
/// - allows merging of infinite streams, the result stream will have at most the length of the upstream stream
/// - gatherer is sequential
///
/// ex: 
/// 
///  **tStream.map(...).gather(zipWith((t,u) -> combineTAndUFunc, uStream)).map(r -> handle combined elem)..... etc.**
///
/// ### Termination criteria for gatherer based zippers
/// - The combined stream pipeline terminates when the shortest stream is terminated, this is the default.
/// - Default operation can be overridden using WHEN_AT_LEAST_ONE_HAVE_DATA, which terminates when all streams are empty
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
///  **stream.collect(zipped(Collectors.toList(), (t,u) -> combine(t,u), streamU))** 
/// - the list will be a list of results from the calls to combine
///
/// ### Termination criteria for gatherer based zippers
/// - The combined collector terminates when the shortest stream is terminated, this is the default.
/// - Default operation can be overridden using WHEN_AT_LEAST_ONE_HAVE_DATA, which terminates when all streams are empty
/// - The combined collector are always under control by the upstream, when empty upstream the combined collector is terminated
/// 
public class Zippers {


    public enum ZipWhen {
        /**
         * The zipping will proceed only when all underlying streams can advance
         */
        WHEN_ALL_HAVE_DATA,
        /**
         * The zipping will proceed when at least one underlying stream can advance.
         * When one stream is exhausted, emit null elements for that stream until the others are exhausted
         */
        WHEN_AT_LEAST_ONE_HAVE_DATA
    }
    
    // Spliterator based zippers used at pipeline start up, zip((t,u) -> combined(t,u), tStream, uStream)...
     
    ///
    /// Builds a stream where elements from two streams are first combined into one before pushed downstream.
    /// 
    /// @param combiner function combining the elements into one
    /// @param tStream the stream of T's
    /// @param uStream the stream of U's
    /// @return a stream with element type R
    /// @param <T> element type of first stream
    /// @param <U> element type of second type
    /// @param <R> combined element
    /// 
    public static <T, U, R> Stream<R> zip(Functions.TwoArgs<T, U, R> combiner, Stream<T> tStream, Stream<U> uStream) {
        return zip(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream);
    }

    ///
    /// Builds a stream where elements from two streams are first combined into one before pushed downstream.
    ///
    /// @param combiner function combining the elements into one
    /// @param zipWhen when to keep on streaming, when all combined streams or at least one stream still have content.  
    /// @param tStream the stream of T's
    /// @param uStream the stream of U's
    /// @return a stream with element type R
    /// @param <T> element type of first stream
    /// @param <U> element type of second type
    /// @param <R> combined element
    ///
    public static <T, U, R> Stream<R> zip(Functions.TwoArgs<T, U, R> combiner, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream) {
        return new MultiZipSpliterator.Zip2<>(tStream, uStream, combiner, zipWhen).stream();
    }

    public static <T, U, V, R> Stream<R> zip(Functions.ThreeArgs<T, U, V, R> combiner, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        return zip(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream);
    }

    public static <T, U, V, R> Stream<R> zip(Functions.ThreeArgs<T, U, V, R> combiner, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        return new MultiZipSpliterator.Zip3<>(tStream, uStream, vStream, combiner, zipWhen).stream();
    }

    public static <T, U, V, W, R> Stream<R> zip(Functions.FourArgs<T, U, V, W, R> combiner, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zip(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream, wStream);
    }

    public static <T, U, V, W, R> Stream<R> zip(Functions.FourArgs<T, U, V, W, R> combiner, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return new MultiZipSpliterator.Zip4<>(tStream, uStream, vStream, wStream, combiner, zipWhen).stream();
    }

    public static <T, U, V, W, X, R> Stream<R> zip(Functions.FiveArgs<T, U, V, W, X, R> combiner, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zip(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, tStream, uStream, vStream, wStream, xStream);
    }

    public static <T, U, V, W, X, R> Stream<R> zip(Functions.FiveArgs<T, U, V, W, X, R> combiner, ZipWhen zipWhen, Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return new MultiZipSpliterator.Zip5<>(tStream, uStream, vStream, wStream, xStream, combiner, zipWhen).stream();
    }

    public static <R> Stream<R> zipX(Functions.ArgsInArrayFunction<R> combiner, Stream<?>... streams) {
        return new MultiZipSpliterator.ZipX<>(streams, combiner, ZipWhen.WHEN_ALL_HAVE_DATA).stream();
    }

    public static <R> Stream<R> zipX(Functions.ArgsInArrayFunction<R> combiner, ZipWhen zipWhen, Stream<?>... streams) {
        return new MultiZipSpliterator.ZipX<>(streams, combiner, zipWhen).stream();
    }

    // Gatherer based zippers, used in the midst of a chain, tStream.gather(zipWith((t,u) -> combine(t,u), uStream)).....

    ///
    /// Builds a gatherer where elements from upstream and one other stream is merged before pushed downstream.
    ///
    /// - The gatherer is ofSequential type.
    /// - When the upstream is empty merging is stopped
    ///
    /// @param combiner function combining the elements into one
    /// @param uStream the stream of U's
    /// @return a stream with element type R
    /// @param <T> element type of upstream
    /// @param <U> element type of stream to merge into pipeline
    /// @param <R> combined element
    ///
    public static <T, U, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.TwoArgs<T, U, R> combiner, Stream<U> uStream) {
        return zipWith(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream);
    }

    ///
    /// Builds a gatherer where elements from upstream and one other stream is merged before pushed downstream.
    ///
    /// - The gatherer is ofSequential type.
    /// - When the upstream is empty merging is stopped
    ///   
    /// @param combiner function combining the elements into one
    /// @param zipWhen when to keep on streaming, when all combined streams or at least one stream still have content.  
    /// @param uStream the stream of U's
    /// @return a stream with element type R
    /// @param <T> element type of upstream
    /// @param <U> element type of stream to merge into pipeline
    /// @param <R> combined element
    ///
    public static <T, U, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.TwoArgs<T, U, R> combiner, ZipWhen zipWhen, Stream<U> uStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream}, toArrayArgs(combiner), zipWhen));
    }

    public static <T, U, V, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.ThreeArgs<T, U, V, R> combiner, Stream<U> uStream, Stream<V> vStream) {
        return zipWith(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream);
    }

    public static <T, U, V, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.ThreeArgs<T, U, V, R> combiner, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream}, toArrayArgs(combiner), zipWhen));
    }

    public static <T, U, V, W, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FourArgs<T, U, V, W, R> combiner, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zipWith(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream);
    }

    public static <T, U, V, W, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FourArgs<T, U, V, W, R> combiner, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream, wStream}, toArrayArgs(combiner), zipWhen));
    }

    public static <T, U, V, W, X, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FiveArgs<T, U, V, W, X, R> combiner, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zipWith(combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream, xStream);
    }

    public static <T, U, V, W, X, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipWith(Functions.FiveArgs<T, U, V, W, X, R> combiner, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zipGatherer(() -> new MultiZipGatherBody<>(new Stream<?>[]{uStream, vStream, wStream, xStream}, toArrayArgs(combiner), zipWhen));
    }

    private static <T, R> Gatherer<T, MultiZipGatherBody<T, R>, R> zipGatherer(Supplier<MultiZipGatherBody<T, R>> multiZipGatherBodySupplier) {
        return Gatherer.ofSequential(
        multiZipGatherBodySupplier,
        MultiZipGatherBody::integrate,
        MultiZipGatherBody::finish
        );
    }

    // Collector based zippers, stream.collect(zipped(Collectors.toList(), (t,u) -> combine(t,u), streamU))

    ///
    /// Builds a Collector which combines input from upstream and uStream and feeds a given collector with the combined input.
    /// - When the upstream is empty merging or combining is stopped
    ///
    /// @param collectorToExtend collector which will be called with elements of Combined
    /// @param combiner function combining the elements into one
    /// @param uStream the stream to combine element with
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
    zipped(C collectorToExtend, Functions.TwoArgs<T, U, Combined> combiner, Stream<U> uStream) {
        return zipped(collectorToExtend, combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream);
    }

    ///
    /// Builds a Collector which combines input from upstream and uStream and feeds a given collector with the combined input.
    /// - When the upstream is empty merging or combining is stopped
    ///  
    /// @param collectorToExtend collector which will be called with elements of Combined
    /// @param combiner function combining the elements into one
    /// @param zipWhen when to keep on streaming, when all combined streams or at least one stream still have content.
    /// @param uStream the stream to combine element with
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
    zipped(C collectorToExtend, Functions.TwoArgs<T, U, Combined> combiner, ZipWhen zipWhen, Stream<U> uStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream},
                toArrayArgs(combiner),
                zipWhen
        );
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.ThreeArgs<T, U, V, Combined> combiner, Stream<U> uStream, Stream<V> vStream) {
        return zipped(collectorToExtend, combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream);
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.ThreeArgs<T, U, V, Combined> combiner, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream},
                toArrayArgs(combiner),
                zipWhen
        );
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FourArgs<T, U, V, W, Combined> combiner, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return zipped(collectorToExtend, combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream);
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FourArgs<T, U, V, W, Combined> combiner, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream, wStream},
                toArrayArgs(combiner),
                zipWhen
        );
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W, X>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FiveArgs<T, U, V, W, X, Combined> combiner, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return zipped(collectorToExtend, combiner, ZipWhen.WHEN_ALL_HAVE_DATA, uStream, vStream, wStream, xStream);
    }

    public static <T, Combined, R, Accumulator, C extends Collector<Combined, Accumulator, R>, U, V, W, X>
    Collector<T, MultiZipCollector.ExtendedState<Accumulator>, R>
    zipped(C collectorToExtend, Functions.FiveArgs<T, U, V, W, X, Combined> combiner, ZipWhen zipWhen, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream) {
        return new MultiZipCollector<>(
                collectorToExtend,
                new Stream<?>[]{uStream, vStream, wStream, xStream},
                toArrayArgs(combiner),
                zipWhen
        );
    }

}
