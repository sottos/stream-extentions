package org.example.mains;

import java.util.stream.Stream;

import static org.example.simple.Zippers.ZipWhen.WHEN_AT_LEAST_ONE_HAVE_DATA;
import static org.example.simple.Zippers.zip2;
import static org.example.simple.Zippers.zip3;

public class ZipWithIndexUsingSpliterator {
    public static void main(String[] args) {
        System.out.println("Using spliterators");
        stringWithIndexBounded();
        stringWithIndexUnBounded(6);
        stringWithDoubleIndexBounded();
        stringWithDoubleIndexUnBounded(2);
        stringWithDoubleIndexUnBounded(5);
    }

    private static void stringWithIndexBounded() {
        System.out.println("Join two stream to a 2-tuple stream, terminate when shortest stream terminates");

        zip2(
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                Stream.iterate(1, x -> x + 1))
                .stream()
                .forEach(System.out::println);
    }

    private static void stringWithIndexUnBounded(int n) {
        System.out.println("Join two streams to a 2-tuple stream, terminate after " + n + " stream extractions");

        zip2(
                Stream.iterate(1, x -> x + 1),
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                WHEN_AT_LEAST_ONE_HAVE_DATA)
                .stream()
                .limit(n)
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexBounded() {
        System.out.println("Join three streams to a 3-tuple stream, terminate when shortest stream is empty");

        zip3(
                Stream.iterate(1, x -> x + 1),
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                Stream.of(11, 12, 13))
                .stream()
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexUnBounded(int n) {
        System.out.println("Join three streams to a 3-tuple stream, terminate after " + n + " stream extractions");

        zip3(
                Stream.of(11, 12, 13),
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                Stream.iterate(1, x -> x + 1),
                WHEN_AT_LEAST_ONE_HAVE_DATA)
                .stream()
                .limit(n)
                .forEach(System.out::println);
    }
}
