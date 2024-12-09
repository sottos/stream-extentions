package org.example.simple;

import org.example.general.Tup2;

import java.util.Iterator;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class ZipGatherer<T, U> {

    final Iterator<U> uIterator;
    final Zippers.ZipWhen zipWhen;

    public ZipGatherer(Stream<U> uStream, Zippers.ZipWhen zipWhen) {
        this.zipWhen = zipWhen;
        this.uIterator = uStream.iterator();
    }


    /**
     * Will be called when a T - element from the upStream is ready to be sent further down the stream pipeline.
     * Will add the T element and a U element to a Tup2 and send that to the downstream
     *
     * @param tElement
     * @param downstream
     * @return
     */
    boolean integrate(T tElement, Gatherer.Downstream<? super Tup2<T, U>> downstream) {
        if (!downstream.isRejecting()) {
            if (uIterator.hasNext()) {
                return downstream.push(new Tup2<>(tElement, uIterator.next()));
            } else if (zipWhen == Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA) {
                // More left of tStream since we have tElement
                return downstream.push(new Tup2<>(tElement, null));
            }
        }
        return false;
    }

    /**
     * Will be called when either downStream.isRejecting or there are no more tElement to call integrate with.
     *
     * @param downstream
     */
    public void finish(Gatherer.Downstream<? super Tup2<T, U>> downstream) {
        if (zipWhen == Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA) {
            while (!downstream.isRejecting()
                    && uIterator.hasNext()
                    && downstream.push(new Tup2<>(null, uIterator.next()))) {
            }
        }
    }

}
