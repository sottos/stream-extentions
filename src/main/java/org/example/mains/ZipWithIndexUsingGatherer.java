package org.example.mains;

import org.example.IntGenerator;
import org.example.simple.ZipWhen;

import java.util.stream.Stream;

import static org.example.simple.ZipGatherer.*;

public class ZipWithIndexUsingGatherer {
    public static void main(String[] args) {
        stringWithIndexBounded();
        stringWithIndexUnBounded(6);
        stringWithDoubleIndexBounded();
        stringWithDoubleIndexUnBounded(2);
        stringWithDoubleIndexUnBounded(5);
    }
    private static void stringWithIndexBounded() {
        System.out.println("Join two stream to a 2-tuple stream, terminate when shortest stream terminates");

        //IntGenerator.generate(1, x -> x + 1)
        Stream.iterate(1, x -> x + 1)
                .gather(zip(Stream.of("Per", "Pål", "Espen", "Prinsesse")))
                .limit(2)
                .forEach(System.out::println);

        Stream.of("Per", "Pål", "Espen", "Prinsesse")
                .gather(zip(Stream.iterate(1, x -> x + 1)))
                .forEach(System.out::println);
    }

    private static void stringWithIndexUnBounded(int n) {
        System.out.println("Join two streams to a 2-tuple stream, terminate after "+n+" stream extractions");

        Stream.iterate(1, x -> x + 1)
                .gather(zip(Stream.of("Per", "Pål", "Espen", "Prinsesse"), ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE))
                .limit(n).forEach(System.out::println);
    }

    private static void stringWithDoubleIndexBounded() {
        System.out.println("Join three streams to a 3-tuple stream, terminate when shortest stream is empty");

//        Stream.iterate(1, x -> x + 1)
//                .gather(zip(Stream.of("Per", "Pål", "Espen", "Prinsesse"))
//                        .andThen(zip(Stream.of(11, 12, 13))))
//                .forEach(System.out::println);

        Stream.iterate(1, x -> x + 1)
                .gather(zip(Stream.of("Per", "Pål", "Espen", "Prinsesse")))
                .gather(zip(Stream.of(11, 12, 13)))
                .forEach(System.out::println);
    }

    private static void stringWithDoubleIndexUnBounded(int n) {
        System.out.println("Join three streams to a 3-tuple stream, terminate after "+n+" stream extractions");

        Stream.of(11, 12, 13)
                        .gather(zip(Stream.of("Per", "Pål", "Espen", "Prinsesse"),ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE)
                                        .andThen(zip(Stream.iterate(1, x -> x + 1),ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE)
                                        ))
                .limit(n).forEach(System.out::println);
    }
}
