package org.example.simple;

import org.example.general.Tup2;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.simple.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

class ZipSpliterator<T, U> implements Spliterator<Tup2<T, U>> {

    private final StreamState<T> firstStreamState;
    private final StreamState<U> secondStreamState;
    private final Zippers.ZipWhen zipWhen;

    StreamState<T> getFirstStreamState() {
        return firstStreamState;
    }

    StreamState<U> getSecondStreamState() {
        return secondStreamState;
    }

    public ZipSpliterator(Spliterator<T> firstSpliterator, Spliterator<U> secondSpliterator, Zippers.ZipWhen zipWhen) {
        this.firstStreamState = new StreamState<>(firstSpliterator);
        this.secondStreamState = new StreamState<>(secondSpliterator);
        this.zipWhen = zipWhen;
    }

    /**
     * @return whatever there is left of the inner streams, if both exhausted then there will be returned a pair of two empty streams.
     */
    public Tup2<Stream<T>, Stream<U>> resultStreams() {
        return new Tup2<>(firstStreamState.restOfStream.get(), secondStreamState.restOfStream.get());
    }


    @Override
    public boolean tryAdvance(Consumer<? super Tup2<T, U>> action) {

        var firstCanAdvance = firstStreamState.tryAdvance();
        var secondCanAdvance = secondStreamState.tryAdvance();

        if (!firstCanAdvance && !secondCanAdvance) {
            return false;
        }
        if (firstCanAdvance != secondCanAdvance) {
            if (zipWhen == WHEN_ALL_HAVE_DATA) {
                if (firstCanAdvance) {
                    firstStreamState.restOfStream = () -> Stream.concat(
                            Stream.of(firstStreamState.lastElem),
                            StreamSupport.stream(firstStreamState.spliterator, false));
                } else {
                    secondStreamState.restOfStream = () -> Stream.concat(
                            Stream.of(secondStreamState.lastElem),
                            StreamSupport.stream(secondStreamState.spliterator, false));
                }
                return false;
            }
        }

        action.accept(new Tup2<>(firstStreamState.lastElem, secondStreamState.lastElem));

        return true;

    }

    @Override
    public Spliterator<Tup2<T, U>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Math.min(firstStreamState.spliterator.estimateSize(), secondStreamState.spliterator.estimateSize());
    }

    @Override
    public int characteristics() {
        return firstStreamState.spliterator.characteristics() & secondStreamState.spliterator.characteristics()
                & ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.CONCURRENT);
    }

    static class StreamState<X> {
        Spliterator<X> spliterator;
        boolean canAdvance = true;
        Supplier<Stream<X>> restOfStream = () -> StreamSupport.stream(spliterator, false);

        StreamState(Spliterator<X> spliterator) {
            this.spliterator = spliterator;
        }

        boolean tryAdvance() {
            if (canAdvance) {
                canAdvance = spliterator.tryAdvance(e -> this.lastElem = e);
            }
            if (!canAdvance) {
                restOfStream = Stream::empty;
                lastElem = null;
            }
            return canAdvance;
        }

        X lastElem;
    }

}
