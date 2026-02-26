package no.sottos.zippers;

import java.util.Arrays;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static no.sottos.zippers.Functions.toArrayArgs;
import static no.sottos.zippers.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

class MultiZipSpliterator<R> implements Spliterator<R> {

    private final StreamState<?>[] streamStates;
    private final Zippers.ZipWhen zipWhen;
    private final Functions.ArgsInArrayFunction<R> combiner;

    public MultiZipSpliterator(Spliterator<?>[] spliterators, Functions.ArgsInArrayFunction<R> combiner, Zippers.ZipWhen zipWhen) {
        streamStates = new StreamState[spliterators.length];
        for (int i = 0; i < streamStates.length; ++i) {
            streamStates[i] = new StreamState<>(spliterators[i]);
        }
        this.zipWhen = zipWhen;
        this.combiner = combiner;
    }

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        boolean atLeastOneCanAdvance = false;
        boolean allCanAdvance = true;
        Object[] streamValues = new Object[streamStates.length];
        for (int i = 0; i < streamStates.length; i++) {
            var streamState = streamStates[i];
            boolean canAdvance = streamState.tryAdvance();
            atLeastOneCanAdvance = atLeastOneCanAdvance || canAdvance;
            allCanAdvance = allCanAdvance && canAdvance;
            streamValues[i] = streamState.lastElem;
        }
        if (!atLeastOneCanAdvance) {
            return false;
        }
        if (!allCanAdvance) {
            if (zipWhen == WHEN_ALL_HAVE_DATA) {
                return false;
            }
        }

        action.accept(combiner.apply(streamValues));

        return true;

    }

    @Override
    public Spliterator<R> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        OptionalLong min = Arrays.stream(streamStates).mapToLong(s -> s.spliterator.estimateSize()).min();
        return min.orElse(0);
    }

    @Override
    public int characteristics() {

        int mask = ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.CONCURRENT);
        for (var streamState : streamStates) {
            mask &= streamState.spliterator.characteristics();
        }
        return mask;
    }

    static class StreamState<X> {
        Spliterator<X> spliterator;
        boolean canAdvance = true;

        StreamState(Spliterator<X> spliterator) {
            this.spliterator = spliterator;
        }

        boolean tryAdvance() {
            if (canAdvance) {
                canAdvance = spliterator.tryAdvance(e -> this.lastElem = e);
            }
            if (!canAdvance) {
                lastElem = null;
            }
            return canAdvance;
        }

        X lastElem;
    }

    // Spliterator Zippers helper classes
    private static abstract class Zip<R> {
        MultiZipSpliterator<R> zipper;

        private Zip(Stream<?>[] streams, Functions.ArgsInArrayFunction<R> combiner, Zippers.ZipWhen zipWhen) {
            zipper = new MultiZipSpliterator<>
                    (Arrays.stream(streams).map(s -> (Spliterator<?>) s.spliterator()).toArray(Spliterator<?>[]::new),
                            combiner,
                            zipWhen);
        }

        public Stream<R> stream() {
            return StreamSupport.stream(zipper, false);
        }
    }

    static class Zip2<T, U, R> extends Zip<R> {
        Zip2(Stream<T> tStream, Stream<U> uStream, Functions.TwoArgs<T, U, R> combiner, Zippers.ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream},
                    toArrayArgs(combiner),
                    zipWhen);
        }
    }

    static class Zip3<T, U, V, R> extends Zip<R> {
        Zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Functions.ThreeArgs<T, U, V, R> combiner, Zippers.ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream, vStream},
                    toArrayArgs(combiner),
                    zipWhen);
        }
    }

    static class Zip4<T, U, V, W, R> extends Zip<R> {
        Zip4(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Functions.FourArgs<T, U, V, W, R> combiner, Zippers.ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream, vStream, wStream},
                    toArrayArgs(combiner),
                    zipWhen);
        }
    }

    static class Zip5<T, U, V, W, X, R> extends Zip<R> {
        Zip5(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Stream<W> wStream, Stream<X> xStream, Functions.FiveArgs<T, U, V, W, X, R> combiner, Zippers.ZipWhen zipWhen) {
            super(new Stream<?>[]{tStream, uStream, vStream, wStream, xStream},
                    toArrayArgs(combiner),
                    zipWhen);
        }
    }

    static class ZipX<R> extends Zip<R> {
        ZipX(Stream<?>[] streams, Functions.ArgsInArrayFunction<R> combiner, Zippers.ZipWhen zipWhen) {
            super(streams, combiner, zipWhen);
        }
    }
}
