package no.sottos.zippers;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.sottos.zippers.Zippers.zipped;
import static no.sottos.zippers.ZippersTest.NumberedString.newNS;
import static no.sottos.zippers.ZippersTest.Tup3.nTup3;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ZippersTest {
    @Test
    void zipperTest2() {
        // Join two streams to a 2-tuple stream, terminate when shortest stream terminates
        var expectedResult = List.of(
                newNS(1, "One"),
                newNS(2, "Two"),
                newNS(3, "Three"),
                newNS(4, "Princess"));

        var ctorZipped = Zippers.zip(
                        NumberedString::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("One", "Two", "Three", "Princess")
                )
                .toList();
        assertEquals(expectedResult, ctorZipped);

        var gatherZipped = Stream.of("One", "Two", "Three", "Princess")
                .gather(Zippers.zipWith(
                                (s, i) -> new NumberedString(i, s),
                                Stream.iterate(1, x -> x + 1)
                        )
                )
                .toList();
        assertEquals(gatherZipped, ctorZipped);


        var collectorZipped = Stream.of("One", "Two", "Three", "Princess")
                .collect(Zippers.zipped(
                                Collectors.toList(),
                                (s, i) -> new NumberedString(i, s),
                                Stream.iterate(1, x -> x + 1)
                        )
                );
        assertEquals(collectorZipped, ctorZipped);
    }

    @Test
    void zipperTest3() {
        // Join two streams to a 3-tuple stream, terminate when shortest stream terminates
        var expectedResult = List.of(
                nTup3("One", 1, "OneAndAHalf"),
                nTup3("Two", 2, "OneAndAHalf"),
                nTup3("Three", 3, "King"));

        var ctorZipped = Zippers.zip(
                        Tup3<String, Integer, String>::new,
                        Stream.of("One", "Two", "Three", "Princess"),
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("OneAndAHalf", "OneAndAHalf", "King")
                )
                .toList();
        assertEquals(expectedResult, ctorZipped);

        var gatherZipped = Stream.of("One", "Two", "Three", "Princess")
                .gather(Zippers.zipWith(
                        Tup3<String, Integer, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("OneAndAHalf", "OneAndAHalf", "King"))
                )
                .toList();
        assertEquals(expectedResult, gatherZipped);


        var collectorZipped = Stream.of("One", "Two", "Three", "Princess")
                .collect(zipped(
                        Collectors.toList(),
                        Tup3<String, Integer, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("OneAndAHalf", "OneAndAHalf", "King")
                ));
        assertEquals(expectedResult, collectorZipped);

        var collectorZipped2 = Stream.of("One", "Two", "Three", "Princess")
                .collect(zipped(
                        Collectors.toList(),
                        Tup3<String, Integer, String>::new,
                        Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("OneAndAHalf", "OneAndAHalf", "King")
                ));
        assertEquals(4, collectorZipped2.size());
        assertEquals(nTup3("Princess", 4, null), collectorZipped2.getLast());
    }

    /// Join three streams of dataelements into one stream of the combination of these elements
    @Test
    void testJoinSomeDataLists() {
        // The combiner function, creating a result from elements from the different streams
        Functions.FourArgs<Integer, Integer, Integer, String, String> combineElements =
                (number, hen, cat, house) ->
                        String.format("Number %d has %d hens, %d cats and a %s house", number, hen, cat, house);

        var expectedResult = List.of(
                "Number 1 has 3 hens, 4 cats and a red house",
                "Number 2 has 8 hens, 0 cats and a green house",
                "Number 3 has 0 hens, 8 cats and a blue house");

        var hens = List.of(3, 8, 0);
        var cats = List.of(4, 0, 8);
        var houses = List.of("red", "green", "blue");

        var zipResult = Zippers.zip(
                        combineElements,
                        Stream.iterate(1, x -> x + 1),
                        hens.stream(),
                        cats.stream(),
                        houses.stream())
                .toList();
        assertEquals(expectedResult, zipResult);

        // You can use an infinite stream as upstream to the zipWith gatherer, it does not collect anything
        var zipWithResult = Stream.iterate(1, x -> x + 1)
                .gather(Zippers.zipWith(
                        combineElements,
                        hens.stream(),
                        cats.stream(),
                        houses.stream()))
                .toList();
        assertEquals(expectedResult, zipWithResult);

        // Do not use an infinite stream as upstream to the collect method, then infinite loop.
        // This behavior seems independent on actual collector.
        var zippedResult = hens.stream()
                .collect(Zippers.zipped(
                        Collectors.toList(),
                        // Not same parameter sequence as in 'combinElements' function
                        (hen, number, cat, house) ->
                                String.format("Number %d has %d hens, %d cats and a %s house", number, hen, cat, house),
                        Stream.iterate(1, x -> x + 1),
                        cats.stream(),
                        houses.stream()));
        assertEquals(expectedResult, zippedResult);

        var collZipped = Stream.iterate(1, x -> x + 1)
                .limit(3) // Need to stop on at least 3 items, but any number above 3 fits here, zipped terminates
                .collect(Zippers.zipped(
                        Collectors.toList(),
                        combineElements,
                        hens.stream(),
                        cats.stream(),
                        houses.stream()));
        assertEquals(expectedResult, collZipped);
    }

    // Helper classes
    record NumberedString(int i, String s) {
        public static NumberedString newNS(int i, String s) {
            return new NumberedString(i, s);
        }
    }

    record Tup3<A, B, C>(A a, B b, C c) {
        public static <A, B, C> Tup3<A, B, C> nTup3(A a, B b, C c) {
            return new Tup3<>(a, b, c);
        }
    }

}