package org.example.simple;

import org.example.general.Tup2;

import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.simple.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

public class Zip2<T,U,V> {
    private final Stream<T> tStream;
    private final Stream<U> uStream;
    private final Zippers.ZipWhen zipWhen;
    private final BiFunction<T,U,V> tupCreator;
    private ZipSpliterator<T,U,V> zipper;

    public Zip2(Stream<T> tStream, Stream<U> uStream, BiFunction<T,U,V> tupCreator ) {
        this(tStream, uStream, tupCreator, WHEN_ALL_HAVE_DATA);
    }

    public Zip2(Stream<T> tStream, Stream<U> uStream, BiFunction<T,U,V> tupCreator, Zippers.ZipWhen zipWhen) {
        this.tStream = tStream;
        this.uStream = uStream;
        this.tupCreator = tupCreator;
        this.zipWhen = zipWhen;
    }

    /**
     * @return what is left of the InnerStreams
     */
    public Tup2<Stream<T>, Stream<U>> resultStreams() {
        return zipper.resultStreams();
    }

    /**
     * Start streaming, based on the inner streams.
     * Can be called at most once
     *
     * @return a stream producing elements which are tuples of the inner streams values
     */
    public Stream<V> stream() {
        if (zipper != null) {
            throw new IllegalStateException("Cannot start streaming twice");
        }
        zipper = new ZipSpliterator<>(tStream.spliterator(), uStream.spliterator(), tupCreator, zipWhen);
        return StreamSupport.stream(zipper, false);
    }

}
