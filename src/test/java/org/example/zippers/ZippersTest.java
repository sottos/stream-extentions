package org.example.zippers;

import org.example.general.NumberedString;
import org.example.general.Tup3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.zippers.Zippers.zipped;
import static org.example.zippers.Zippers.zipWith;


class ZippersTest {

    @Test
    void zipperTest2() {
        System.out.println("Join two streams to a 2-tuple stream, terminate when shortest stream terminates");

        Zippers.zip(NumberedString::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                )
                .stream()
                .forEach(System.out::println);

        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith((s, i) -> new NumberedString(i, s), Stream.iterate(1, x -> x + 1)))
                .forEach(System.out::println);


        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipped(Collectors.toList(), (s, i) -> new NumberedString(i, s), Stream.iterate(1, x -> x + 1)))
                .forEach(System.out::println);
    }
    @Test
    void zipperTest3() {
        System.out.println("Join two streams to a 3-tuple stream, terminate when shortest stream terminates");

        Zippers.zip(Tup3<Integer,String, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                        Stream.of("Hansen", "Hansen", "Nilsen")
                )
                .stream()
                .forEach(System.out::println);

        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith(Tup3<String, Integer, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Olsen",  "Nilsen", "Konug")))
                .forEach(System.out::println);


        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .collect(zipped(
                        Collectors.toList(),
                        Tup3<String, Integer, String>::new,
                        Stream.iterate(1, x -> x + 1),
                        Stream.of("Olsen","AnnasSon","Hansen","Nilsen","Sigurdsson")))
                .forEach(System.out::println);

    }
}