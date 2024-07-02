package org.example;

import org.example.PatternMatching.Tup2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptyList;

public class SpliteratorEx {

    Zipper.ExhaustMode exhaustMode = Zipper.ExhaustMode.STOP_ON_SHORTEST;
    public static void main(String[] args) {
        var ex = new SpliteratorEx();
        ex.showEx();
        ex.exhaustMode = Zipper.ExhaustMode.STOP_ON_LONGEST;
        ex.showEx();
    }

    private void showEx() {
        List<String> list1 = Arrays.asList("a", "b", "c", "d", "e");
        List<Integer> list2 = Arrays.asList(1, 2, 3);

        zipFirstOnly(list1, list2);
        zipFirstOnly(list2, list1);
        zipFirstOnly(list2, List.of("en", "to", "tre"));
        zipFirstOnly(list2, Arrays.asList("en", null, "to", "tre"));
        zipFirstOnly(list2, emptyList());
        zipFirstOnly(emptyList(), list2);
        zipFirstOnly(emptyList(), emptyList());


        zipAndShow(list1, list2);
        zipAndShow(list2, list1);
        zipAndShow(list2, List.of("en", "to", "tre"));
        zipAndShow(list2, Arrays.asList("en", null, "to", "tre"));
        zipAndShow(list2, emptyList());
        zipAndShow(emptyList(), list2);
        zipAndShow(emptyList(), emptyList());
    }

    private <T, U> void zipAndShow(Collection<T> tCollection, Collection<U> uCollection) {
        System.out.println("StartShow with "  + tCollection + " and " + uCollection);

        var zipper = new Zipper<>(tCollection.stream(), uCollection.stream(), exhaustMode);
        zipper.stream().forEach(System.out::println);

        System.out.println("First left:");
        zipper.resultStreams().a().forEach(System.out::println);
        System.out.println("Second left:");
        zipper.resultStreams().b().forEach(System.out::println);
        System.out.println("Finished");
    }

    private <T, U> void zipFirstOnly(Collection<T> tCollection, Collection<U> uCollection) {
        System.out.println("StartFirstOnly with " + tCollection + " and " + uCollection);

        var zipper = new Zipper<>(tCollection.stream(), uCollection.stream(), exhaustMode);
        zipper.stream().findAny().ifPresentOrElse(System.out::println, () -> System.out.println("No item"));

        System.out.println("First left:");
        zipper.resultStreams().a().forEach(System.out::println);
        System.out.println("Second left:");
        zipper.resultStreams().b().forEach(System.out::println);
        System.out.println("Finished");
    }


    public static class Zipper<T, U> {
        public enum ExhaustMode {
            STOP_ON_SHORTEST,
            STOP_ON_LONGEST // when short is exhausted, then emit tuples where one element is certainly null
        }

        private final Stream<T> tStream;
        private final Stream<U> uStream;
        private final ExhaustMode exhaustMode;
        private ZipSpliterator zipper;

        public Zipper(Stream<T> tStream, Stream<U> uStream) {
            this(tStream, uStream, ExhaustMode.STOP_ON_SHORTEST);
        }

        public Zipper(Stream<T> tStream, Stream<U> uStream, ExhaustMode exhaustMode) {
            this.tStream = tStream;
            this.uStream = uStream;
            this.exhaustMode = exhaustMode;
        }

        /**
         * @return what is left of the InnerStreams
         */
        public Tup2<Stream<T>, Stream<U>> resultStreams() {
            return new Tup2<>(zipper.firstState.restOfStream.get(), zipper.secondState.restOfStream.get());
        }

        /**
         * Start streaming, based on the inner streams.
         * Can be called at most once
         * @return a stream producing elements which are tuples of the inner streams values
         */
        public Stream<Tup2<T, U>> stream() {
            if (zipper != null) {
                throw new IllegalStateException("Cannot stream twice");
            }
            zipper = new ZipSpliterator(tStream.spliterator(), uStream.spliterator());
            return StreamSupport.stream(zipper, false);
        }

        private class ZipSpliterator implements Spliterator<Tup2<T, U>> {

            private static class State<X> {
                Spliterator<X> spliterator;
                boolean isExhausted;
                Supplier<Stream<X>> restOfStream = () -> StreamSupport.stream(spliterator, false);

                State(Spliterator<X> spliterator) {
                    this.spliterator = spliterator;
                }

                boolean tryAdvance() {
                    if (!isExhausted) {
                        isExhausted = !spliterator.tryAdvance(e -> this.lastElem = e);
                    }
                    if (isExhausted) {
                        restOfStream = Stream::empty;
                        lastElem = null;
                    }
                    return !isExhausted;
                }

                X lastElem;
            }

            private final State<T> firstState;
            private final State<U> secondState;


            public ZipSpliterator(Spliterator<T> firstSpliterator, Spliterator<U> secondSpliterator) {
                this.firstState = new State<>(firstSpliterator);
                this.secondState = new State<>(secondSpliterator);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean tryAdvance(Consumer<? super Tup2<T, U>> action) {

                var firstExhausted = !firstState.tryAdvance();
                var secondExhausted = !secondState.tryAdvance();

                if (firstExhausted && secondExhausted) {
                    return false;
                }
                if (firstExhausted != secondExhausted) {
                    if (exhaustMode == ExhaustMode.STOP_ON_SHORTEST) {
                        if (firstExhausted) {
                            secondState.restOfStream = () -> Stream.concat(
                                    Stream.of(secondState.lastElem),
                                    StreamSupport.stream(secondState.spliterator, false));
                        } else {
                            firstState.restOfStream = () -> Stream.concat(
                                    Stream.of(firstState.lastElem),
                                    StreamSupport.stream(firstState.spliterator, false));
                        }
                        return false;
                    }
                }

                action.accept(new Tup2<>(firstState.lastElem, secondState.lastElem));

                return true;

            }

            @Override
            public Spliterator<Tup2<T, U>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Math.min(firstState.spliterator.estimateSize(), secondState.spliterator.estimateSize());
            }

            @Override
            public int characteristics() {
                return firstState.spliterator.characteristics() & secondState.spliterator.characteristics()
                        & ~(Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.CONCURRENT);
            }
        }
    }
}
