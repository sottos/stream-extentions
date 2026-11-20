package no.sottos.zippers;

import no.sottos.zippers.Zippers.ZipUntil;

import java.util.Iterator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static no.sottos.zippers.Zippers.ZipUntil.UNTIL_ALL_STREAMS_END;

public class MultiZipGatherBody<T, R> {

    final Iterator<?>[] iterators;
    final Functions.ArgsInArrayFunction<R> combiner;
    final ZipUntil until;

    public MultiZipGatherBody(Stream<?>[] streams, Functions.ArgsInArrayFunction<R> combiner, ZipUntil until) {
        this.combiner = combiner;
        this.until = until;
        this.iterators = new Iterator<?>[streams.length];
        for (int i = 0; i < streams.length; ++i) {
            this.iterators[i] = streams[i].iterator();
        }
    }


    /**
     * Will be called when a T - element from the upStream is ready to be sent further down the stream pipeline.
     * Will add the T element and a U element to a Tup2 and send that to the downstream
     */
    public boolean integrate(T tElement, Gatherer.Downstream<? super R> downstream) {
        if (!downstream.isRejecting()) {
            boolean atLeastOneCanAdvance = false;
            boolean allCanAdvance = true;
            Object[] streamValues = new Object[1 + iterators.length];
            streamValues[0] = tElement;
            for (int i = 0; i < iterators.length; i++) {
                var iterator = iterators[i];
                boolean canAdvance = iterator.hasNext();

                atLeastOneCanAdvance = atLeastOneCanAdvance || canAdvance;
                allCanAdvance = allCanAdvance && canAdvance;

                streamValues[i + 1] = canAdvance ? iterator.next() : null;
            }

            if (allCanAdvance) {
                return downstream.push(combiner.apply(streamValues));
            } else if (until == UNTIL_ALL_STREAMS_END) {
                // More left of tStream since we have tElement
                return downstream.push(combiner.apply(streamValues));
            }
        }
        return false;
    }

    /**
     * Will be called when either downStream.isRejecting or there are no more tElement to call integrate with.
     */
    public void finish(Gatherer.Downstream<? super R> downStream) {
        if (until == UNTIL_ALL_STREAMS_END && !downStream.isRejecting()) {
            boolean atLeastOneCanAdvance;
            Object[] streamValues = new Object[1 + iterators.length];
            do {
                atLeastOneCanAdvance = false;
                streamValues[0] = null; // no more left from the upStream, otherwise no call to finish.
                for (int i = 0; i < iterators.length; i++) {
                    var iterator = iterators[i];
                    boolean canAdvance = iterator.hasNext();

                    atLeastOneCanAdvance = atLeastOneCanAdvance || canAdvance;

                    streamValues[i + 1] = canAdvance ? iterator.next() : null;
                }
            }
            while (atLeastOneCanAdvance &&
                    !downStream.isRejecting() &&
                    downStream.push(combiner.apply(streamValues)));
        }
    }

}