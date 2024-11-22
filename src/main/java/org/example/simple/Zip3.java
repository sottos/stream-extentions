package org.example.simple;

import org.example.general.Tup2;
import org.example.general.Tup3;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.simple.ZipWhen.ALL_CAN_ADVANCE;

public class Zip3<T, U, V> {
    private final Stream<T> tStream;
    private final Zip2<U, V> uvZipper;
    private final ZipWhen zipWhen;
    private ZipSpliterator<T, Tup2<U, V>> tuvZipper;

    public Zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        this(tStream, uStream, vStream, ALL_CAN_ADVANCE);
    }

    public Zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, ZipWhen zipWhen) {
        this.tStream = tStream;
        this.uvZipper = new Zip2<>(uStream, vStream, zipWhen);
        this.zipWhen = zipWhen;
    }

    /**
     * @return what is left of the InnerStreams
     */
    public Tup3<Stream<T>, Stream<U>, Stream<V>> resultStreams() {
        Tup2<Stream<T>, Stream<Tup2<U, V>>> resultStreams = tuvZipper.resultStreams();
        ZipSpliterator<U, V> uvZipSpliterator = (ZipSpliterator<U, V>) resultStreams.b().spliterator();
        return new Tup3<>(resultStreams.a(),
                uvZipSpliterator.getFirstStreamState().restOfStream.get(),
                uvZipSpliterator.getSecondStreamState().restOfStream.get());
    }

    /**
     * Start streaming, based on the inner streams.
     * Can be called at most once
     *
     * @return a stream producing elements which are tuples of the inner streams values
     */
    public Stream<Tup3<T, U, V>> stream() {
        if (tuvZipper != null) {
            throw new IllegalStateException("Cannot start streaming twice");
        }
        tuvZipper = new ZipSpliterator<>(tStream.spliterator(), uvZipper.stream().spliterator(), zipWhen);

        return StreamSupport.stream(
                // This anonymous class 'converts' from Tup2<T, Tup2<U,V>> (from tuvZipper) to Tup3<T,U,V>
                new Spliterator<>() {
                    @Override
                    public boolean tryAdvance(Consumer<? super Tup3<T, U, V>> action) {
                        return tuvZipper.tryAdvance(tUv -> {
                                    var uv = tUv.b();
                                    Tup3<T,U,V> tuv = uv == null ?
                                            new Tup3<>(tUv.a(), null, null) :
                                            new Tup3<>(tUv.a(), tUv.b().a(), tUv.b().b());
                                    action.accept(tuv);
                                }
                        );
                    }

                    @Override
                    public Spliterator<Tup3<T, U, V>> trySplit() {
                        return null;
                    }

                    @Override
                    public long estimateSize() {
                        return tuvZipper.estimateSize();
                    }

                    @Override
                    public int characteristics() {
                        return tuvZipper.characteristics();
                    }
                }, false);
    }


}
