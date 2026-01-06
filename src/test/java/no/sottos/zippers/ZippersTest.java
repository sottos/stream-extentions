package no.sottos.zippers;

import org.example.general.Tup;
import org.example.general.Tup3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.sottos.zippers.Zippers.zipWith;
import static no.sottos.zippers.Zippers.zipped;
import static no.sottos.zippers.ZippersTest.NumberedString.newNS;
import static no.sottos.zippers.ZippersTest.Tup3.nTup3;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ZippersTest {

    @Test
    void zipperTest2() {
        // Join two streams to a 2-tuple stream, terminate when shortest stream terminates
        var expectedResult = List.of(newNS(1, "Per"), newNS(2, "Pål"), newNS(3, "Espen"), newNS(4, "Prinsesse"));

        var ctorZipped = Zippers.zip(NumberedString::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                )
                .toList();
        assertEquals(expectedResult, ctorZipped);

        var gatherZipped = Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith((s, i) -> new NumberedString(i, s), Stream.iterate(1, x -> x + 1)))
                .toList();
        assertEquals(gatherZipped, ctorZipped);


        var collectorZipped = Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipped(Collectors.toList(), (s, i) -> new NumberedString(i, s), Stream.iterate(1, x -> x + 1)));
        assertEquals(collectorZipped, ctorZipped);
    }
    @Test
    void zipperTest3() {
        // Join two streams to a 3-tuple stream, terminate when shortest stream terminates
        var expectedResult = List.of(nTup3( "Per",1,"Hansen"), nTup3( "Pål",2,"Hansen"), nTup3( "Espen",3,"Nilsen"));

        var ctorZipped = Zippers.zip(Tup3<String, Integer, String>::new,
                        Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Hansen", "Hansen", "Nilsen")
                )
                .toList();
        assertEquals(expectedResult, ctorZipped);

        var gatherZipped = Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith(Tup3<String, Integer, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Hansen", "Hansen", "Nilsen")))
                .toList();
        assertEquals(expectedResult, gatherZipped);


        var collectorZipped = Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipped(
                        Collectors.toList(),
                        Tup3<String, Integer, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Hansen", "Hansen", "Nilsen")
                ));
        assertEquals(expectedResult, collectorZipped);
        var collectorZipped2 = Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipped(
                        Collectors.toList(),
                        Tup3<String, Integer, String>::new,
                        Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Hansen", "Hansen", "Nilsen")
                ));
        assertEquals(4, collectorZipped2.size());
        assertEquals(nTup3("Prinsesse",4, null), collectorZipped2.getLast());
    }

    record NumberedString(int i, String s) {
        public static NumberedString newNS(int i, String s) {
            return new NumberedString(i, s);
        }
    }
    record Tup3<A, B, C>(A a, B b, C c)  {
        public static <A,B,C> Tup3<A, B, C> nTup3(A a, B b, C c) {
            return new Tup3<>(a, b, c);
        }
    }

}