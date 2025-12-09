package org.example.mains;

import org.example.general.NumberedString;
import org.example.general.StringStringInt;

import java.util.stream.Stream;

import static org.example.simple.Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA;
import static org.example.simple.Zippers.zipWith;

public class ZipWithIndexUsingGatherer {
    public static void main(String[] args) {
        System.out.println("Using gatherers");
        stringWithIndexBounded();
        stringWithIndexUnBounded(6);
        stringWithDoubleIndexBounded();
        stringWithDoubleIndexUnBounded(2);
        stringWithDoubleIndexUnBounded(5);
        numberedStringWithIndexBounded();
        stringStringInt();
    }


    private static void stringStringInt() {
        System.out.println("Join three streams to a StringStringInt stream, terminate when shortest stream terminates");

        Stream
                .of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith(Stream.iterate(1, x -> x + 1), (s, i) -> new NumberedString(i, s)))
                .gather(zipWith(Stream.of("Olsen", "Nilsen", "Pedersen", "Solvik", "Jensen"),
                        (ns, surname) -> new StringStringInt(ns.s(), surname, ns.i())))
                .forEach(System.out::println);
    }

    private static void numberedStringWithIndexBounded() {
        System.out.println("Join two stream to a NumberedString stream , terminate when shortest stream terminates");

        Stream
                .of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith(Stream.iterate(1, x -> x + 1), (s, i) -> new NumberedString(i, s)))
                .forEach(System.out::println);
    }

    private static void stringWithIndexBounded() {
        System.out.println("Join two streams to a 2-tuple stream, terminate when shortest stream terminates");

        Stream
                .of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zipWith(Stream.iterate(1, x -> x + 1)))
                .forEach(System.out::println);
    }

    private static void stringWithIndexUnBounded(int n) {
        System.out.println("Join two streams to a 2-tuple stream, terminate after " + n + " stream extractions");

        Stream
                .iterate(1, x -> x + 1)
                .gather(zipWith(Stream.of("Per", "Pål", "Espen", "Prinsesse"), WHEN_AT_LEAST_ONE_HAVE_DATA))
                .limit(n)
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexBounded() {
        System.out.println("Join three streams to a 3-tuple stream, terminate when shortest stream is empty");

        Stream
                .iterate(1, x -> x + 1)
                .gather(zipWith(Stream.of("Per", "Pål", "Espen", "Prinsesse")))
                .gather(zipWith(Stream.of(11, 12, 13)))
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexUnBounded(int n) {
        System.out.println("Join three streams to a 3-tuple stream, terminate after " + n + " stream extractions");

        Stream
                .of(11, 12, 13)
                .gather(zipWith(Stream.of("Per", "Pål", "Espen", "Prinsesse"), WHEN_AT_LEAST_ONE_HAVE_DATA))
                .gather(zipWith(Stream.iterate(1, x -> x + 1), WHEN_AT_LEAST_ONE_HAVE_DATA))
                .limit(n)
                .forEach(System.out::println);
    }
}
