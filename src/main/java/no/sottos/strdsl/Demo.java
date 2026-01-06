package no.sottos.strdsl;

import java.util.stream.Stream;

public class Demo {
    public static void main(String[] args) {

        var pipeline =
                GathererDSL.G.<String>start()
                        .filter(s -> s.length() > 2)
                        .map(String::toUpperCase)
                        .flatMap(s -> Stream.of(s, s + "!"))
                        .build(); // Gatherer<String, ?, String>

        Stream.of("a", "bb", "ccc", "dddd")
                .gather(pipeline)
                .forEach(System.out::println);
    }
}
