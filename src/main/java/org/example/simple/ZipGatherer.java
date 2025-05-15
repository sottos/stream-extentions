package org.example.simple;

import org.example.general.Tup2;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class ZipGatherer<T, U, V> {

    final Iterator<U> uIterator;
    final BiFunction<T,U,V> tupCreator;
    final Zippers.ZipWhen zipWhen;

    public ZipGatherer(Stream<U> uStream, BiFunction<T, U, V> tupCreator, Zippers.ZipWhen zipWhen) {
        this.tupCreator = tupCreator;
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
    boolean integrate(T tElement, Gatherer.Downstream<? super V> downstream) {
        if (!downstream.isRejecting()) {
            if (uIterator.hasNext()) {
                return downstream.push(tupCreator.apply(tElement, uIterator.next()));
            } else if (zipWhen == Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA) {
                // More left of tStream since we have tElement
                return downstream.push(tupCreator.apply(tElement, null));
            }
        }
        return false;
    }

    /**
     * Will be called when either downStream.isRejecting or there are no more tElement to call integrate with.
     *
     * @param downstream
     */
    public void finish(Gatherer.Downstream<? super V> downstream) {
        if (zipWhen == Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA) {
            while (!downstream.isRejecting()
                    && uIterator.hasNext()
                    && downstream.push(tupCreator.apply(null, uIterator.next()))) {
            }
        }
    }

}
