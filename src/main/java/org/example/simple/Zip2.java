package org.example.simple;

import org.example.general.Tup2;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.simple.ZipWhen.WHEN_ALL_CAN_ADVANCE;

public class Zip2<T,U> {
    private final Stream<T> tStream;
    private final Stream<U> uStream;
    private final ZipWhen zipWhen;
    private ZipSpliterator<T,U> zipper;

    public Zip2(Stream<T> tStream, Stream<U> uStream) {
        this(tStream, uStream, WHEN_ALL_CAN_ADVANCE);
    }

    public Zip2(Stream<T> tStream, Stream<U> uStream, ZipWhen zipWhen) {
        this.tStream = tStream;
        this.uStream = uStream;
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
    public Stream<Tup2<T, U>> stream() {
        if (zipper != null) {
            throw new IllegalStateException("Cannot start streaming twice");
        }
        zipper = new ZipSpliterator<>(tStream.spliterator(), uStream.spliterator(), zipWhen);
        return StreamSupport.stream(zipper, false);
    }

}
