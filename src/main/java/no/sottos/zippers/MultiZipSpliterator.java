package no.sottos.zippers;

import java.util.Arrays;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.function.Consumer;

import static no.sottos.zippers.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

class MultiZipSpliterator<R> implements Spliterator<R> {

    private final StreamState<?>[] streamStates;
    private final Zippers.ZipWhen zipWhen;
    private final Functions.ArgsInArrayFunction<R> tupCreator;

    public MultiZipSpliterator(Spliterator<?>[] spliterators, Functions.ArgsInArrayFunction<R> tupCreator, Zippers.ZipWhen zipWhen) {
        streamStates = new StreamState[spliterators.length];
        for (int i = 0; i < streamStates.length; ++i) {
            streamStates[i] = new StreamState<>(spliterators[i]);
        }
        this.zipWhen = zipWhen;
        this.tupCreator = tupCreator;
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

        action.accept(tupCreator.apply(streamValues));

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
}
