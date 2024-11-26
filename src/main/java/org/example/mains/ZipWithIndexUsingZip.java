package org.example.mains;

import org.example.simple.Zip2;
import org.example.simple.Zip3;
import org.example.simple.ZipWhen;

import java.util.stream.Stream;

public class ZipWithIndexUsingZip {
    public static void main(String[] args) {
        stringWithIndexBounded();
        stringWithIndexUnBounded(6);
        stringWithDoubleIndexBounded();
        stringWithDoubleIndexUnBounded(2);
        stringWithDoubleIndexUnBounded(5);
    }

    private static void stringWithIndexBounded() {
        System.out.println("Join two stream to a 2-tuple stream, terminate when shortest stream terminates");
        var withIndex = new Zip2<>(
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                Stream.iterate(1, x -> x + 1));

        withIndex.stream().forEach(System.out::println);
    }

    private static void stringWithIndexUnBounded(int n) {
        System.out.println("Join two streams to a 2-tuple stream, terminate after "+n+" stream extractions");
        var withIndex = new Zip2<>(
                Stream.iterate(1, x -> x + 1),
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE);

        withIndex.stream().limit(n).forEach(System.out::println);
    }

    private static void stringWithDoubleIndexBounded() {
        System.out.println("Join three streams to a 3-tuple stream, terminate when shortest stream is empty");
        var withIndex = new Zip3<>(
                Stream.iterate(1, x -> x + 1),
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                Stream.of(11, 12, 13));

        withIndex.stream().forEach(System.out::println);
    }

    private static void stringWithDoubleIndexUnBounded(int n) {
        System.out.println("Join three streams to a 3-tuple stream, terminate after "+n+" stream extractions");
        var withIndex = new Zip3<>(
                Stream.of(11, 12, 13),
                Stream.of("Per", "Pål", "Espen", "Prinsesse"),
                Stream.iterate(1, x -> x + 1),
                ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE);

        withIndex.stream().limit(n).forEach(System.out::println);
    }
}
