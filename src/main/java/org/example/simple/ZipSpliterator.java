package org.example.simple;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.example.simple.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

class ZipSpliterator<T, U, V> implements Spliterator<V> {

    private final StreamState<T> firstStreamState;
    private final StreamState<U> secondStreamState;
    private final Zippers.ZipWhen zipWhen;
    private final BiFunction<T, U, V> tupCreator;

    StreamState<T> getFirstStreamState() {
        return firstStreamState;
    }

    StreamState<U> getSecondStreamState() {
        return secondStreamState;
    }

    public ZipSpliterator(Spliterator<T> firstSpliterator, Spliterator<U> secondSpliterator, BiFunction<T, U, V> tupCreator, Zippers.ZipWhen zipWhen) {
        this.firstStreamState = new StreamState<>(firstSpliterator);
        this.secondStreamState = new StreamState<>(secondSpliterator);
        this.zipWhen = zipWhen;
        this.tupCreator = tupCreator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super V> action) {

        var firstCanAdvance = firstStreamState.tryAdvance();
        var secondCanAdvance = secondStreamState.tryAdvance();

        if (!firstCanAdvance && !secondCanAdvance) {
            return false;
        }
        if (firstCanAdvance != secondCanAdvance) {
            if (zipWhen == WHEN_ALL_HAVE_DATA) {
                return false;
            }
        }

        action.accept(tupCreator.apply(firstStreamState.lastElem, secondStreamState.lastElem));

        return true;

    }

    @Override
    public Spliterator<V> trySplit() {
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

}
