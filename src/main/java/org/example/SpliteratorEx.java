package org.example;

import org.example.PatternMatching.Tup2;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SpliteratorEx {

    public static void main(String[] args) {
        List<String> list1 = Arrays.asList("a", "b", "c", "d","e");
        List<Integer> list2 = Arrays.asList(1, 2, 3);

        zipFirstOnly(list1.stream(), list2.stream());
        zipFirstOnly(list2.stream(), list1.stream());
        zipFirstOnly(list2.stream(), Stream.of("en","to","tre"));
        zipFirstOnly(list2.stream(), Stream.empty());
        zipFirstOnly(Stream.empty(), list2.stream());
        zipFirstOnly(Stream.empty(), Stream.empty());


        zipAndShow(list1.stream(), list2.stream());
        zipAndShow(list2.stream(), list1.stream());
        zipAndShow(list2.stream(), Stream.of("en","to","tre"));


    }

    private static <T,U> void zipAndShow(Stream<T> stringStream, Stream<U> integerStream) {
        System.out.println("StartShow");

        var zipper = new Zipper<>(stringStream, integerStream);
        zipper.stream().forEach(tup -> System.out.println("First: " + tup.a() + ", Second: " + tup.b()));

        System.out.println("First left:");
        zipper.resultStreams().a().forEach(System.out::println);
        System.out.println("Second left:");
        zipper.resultStreams().b().forEach(System.out::println);
        System.out.println("Finished");
    }
    private static <T,U> void zipFirstOnly(Stream<T> stringStream, Stream<U> integerStream) {
        System.out.println("StartFirstOnly");

        var zipper = new Zipper<>(stringStream, integerStream);
        zipper.stream().findAny().ifPresentOrElse(tup -> System.out.println("First: " + tup.a() + ", Second: " + tup.b()), () -> System.out.println("No items"));

        System.out.println("First left:");
        zipper.resultStreams().a().forEach(System.out::println);
        System.out.println("Second left:");
        zipper.resultStreams().b().forEach(System.out::println);
        System.out.println("Finished");
    }


    public static class Zipper<T, U> {
        public enum ExhaustMode {
            STOP_ON_SHORTEST,
            STOP_ON_LONGEST // when short is exhausted, then send tuples where one element is certainly null
        }

        private final Stream<T> stream1;
        private final Stream<U> stream2;
        private final ExhaustMode exhaustMode;
        private Tup2<Stream<T>, Stream<U>> resultStreams;

        Zipper(Stream<T> stream1, Stream<U> stream2) {
            this(stream1, stream2, ExhaustMode.STOP_ON_SHORTEST);
        }

        Zipper(Stream<T> stream1, Stream<U> stream2, ExhaustMode exhaustMode) {
            this.stream1 = stream1;
            this.stream2 = stream2;
            this.exhaustMode = exhaustMode;
        }

        Tup2<Stream<T>, Stream<U>> resultStreams() {
            return resultStreams;
        }

        public Stream<Tup2<T, U>> stream() {
            return StreamSupport.stream(new ZipSpliterator(stream1.spliterator(), stream2.spliterator()), false);
        }

        class ZipSpliterator implements Spliterator<Tup2<T, U>> {
            private final Spliterator<T> firstSpliterator;
            private final Spliterator<U> secondSpliterator;

            public ZipSpliterator(Spliterator<T> firstSpliterator, Spliterator<U> secondSpliterator) {
                this.firstSpliterator = firstSpliterator;
                this.secondSpliterator = secondSpliterator;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean tryAdvance(Consumer<? super Tup2<T, U>> action) {
                List<T> firstBackup = new ArrayList<>();
                Tup2<T, U>[] acceptedItem = new Tup2[1];

                var firstReturn = firstSpliterator.tryAdvance(firstItem -> {
                    firstBackup.add(firstItem);
                    var secondReturn = secondSpliterator.tryAdvance(secondItem ->
                            action.accept(acceptedItem[0] = new Tup2<>(firstItem, secondItem)));
                });
                // This check implies both a check on first and second tryAdvance
                if (acceptedItem[0] != null) {
                    resultStreams = new Tup2<>(
                            StreamSupport.stream(firstSpliterator, false),
                            StreamSupport.stream(secondSpliterator, false));
                    return true;
                }
                if (exhaustMode == ExhaustMode.STOP_ON_SHORTEST) {
                    // Finished
                    if (firstBackup.isEmpty()) {
                        resultStreams = new Tup2<>(
                                StreamSupport.stream(firstSpliterator, false),
                                StreamSupport.stream(secondSpliterator, false));
                    } else {
                        resultStreams = new Tup2<>(
                                Stream.concat(Stream.of(firstBackup.getFirst()), StreamSupport.stream(firstSpliterator, false)),
                                StreamSupport.stream(secondSpliterator, false));

                    }
                    return false;
                } else {

                }
            }

            @Override
            public Spliterator<Tup2<T, U>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Math.min(firstSpliterator.estimateSize(), secondSpliterator.estimateSize());
            }

            @Override
            public int characteristics() {
                return firstSpliterator.characteristics() & secondSpliterator.characteristics()
                        & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
            }
        }
    }
}
