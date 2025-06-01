package org.example.zippers;

import org.example.general.NumberedString;
import org.example.general.Tup2;
import org.example.general.Tup3;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.example.simple.Zippers.zip2;
import static org.junit.jupiter.api.Assertions.*;

class ZippersTest {

    @Test
    void zipperTest() {
        System.out.println("Join two streams to a 2-tuple stream, terminate when shortest stream terminates");

        Zippers.zip(NumberedString::new,
                Stream.iterate(1, x -> x + 1),
                Stream.of("Per", "Pål", "Espen", "Prinsesse")
                )
                .stream()
                .forEach(System.out::println);

        Zippers.zip(Tup3<String, Integer, String>::new,
                Stream.of("Olsen", "Hansen"),
                Stream.iterate(1, x -> x + 1),
                Stream.of("Per", "Pål", "Espen", "Prinsesse")
                )
                        .stream()
                        .forEach(System.out::println);

    }

    @Test
    void testZip() {
    }

    @Test
    void testZip1() {
    }

    @Test
    void testZip2() {
    }

    @Test
    void testZip3() {
    }

    @Test
    void testZip4() {
    }

    @Test
    void testZip5() {
    }

    @Test
    void testZip6() {
    }

    @Test
    void zipX() {
    }

    @Test
    void testZipX() {
    }
}