package org.example.simple;

import org.example.general.Tup2;
import org.example.general.Tup3;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.simple.Zippers.ZipWhen.WHEN_ALL_HAVE_DATA;

public class Zip3<T, U, V> {
    private final Stream<T> tStream;
    private final Zip2<U, V,Tup2<U,V>> uvZipper;
    private final Zippers.ZipWhen zipWhen;
    private ZipSpliterator<T, Tup2<U, V>, Tup3<T,U,V>> tuvZipper;

    public Zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream) {
        this(tStream, uStream, vStream, WHEN_ALL_HAVE_DATA);
    }

    public Zip3(Stream<T> tStream, Stream<U> uStream, Stream<V> vStream, Zippers.ZipWhen zipWhen) {
        this.tStream = tStream;
        this.uvZipper = new Zip2<>(uStream, vStream, Tup2::new, zipWhen);
        this.zipWhen = zipWhen;
    }

    /**
     * @return what is left of the InnerStreams
     */
    public Tup3<Stream<T>, Stream<U>, Stream<V>> resultStreams() {
        Tup2<Stream<T>, Stream<Tup2<U, V>>> resultStreams = tuvZipper.resultStreams();
        ZipSpliterator<U, V, Tup2<U, V>> uvZipSpliterator = (ZipSpliterator<U, V,Tup2<U, V>>) resultStreams.b().spliterator();
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
        tuvZipper = new ZipSpliterator<>(tStream.spliterator(), uvZipper.stream().spliterator(), (t,uv) -> new Tup3<>(t,uv.a(), uv.b()), zipWhen);

        return StreamSupport.stream(
                // This anonymous class 'converts' from Tup2<T, Tup2<U,V>> (from tuvZipper) to Tup3<T,U,V>
                new Spliterator<>() {
                    @Override
                    public boolean tryAdvance(Consumer<? super Tup3<T, U, V>> action) {
                        return tuvZipper.tryAdvance(action::accept
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
