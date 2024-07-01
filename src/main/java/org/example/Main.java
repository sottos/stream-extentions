package org.example;

import java.util.stream.Collectors;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static java.lang.StringTemplate.STR;
import static org.example.GatherEx.withIndex;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    static StringTemplate.Processor<String, RuntimeException> MYTEMPLATE = StringTemplate.Processor.of(StringTemplate::interpolate);
//    static StringTemplate.Processor<String, RuntimeException> MYTEMPLATE = StringTemplate.Processor.of(st -> RAW.process(st).interpolate());

    public static void main(String[] args) {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");

        // Press Shift+F10 or click the green arrow button in the gutter to run the code.
        for (int i = 1; i <= 5; i++) {

            // Press Shift+F9 to start debugging your code. We have set one breakpoint
            // for you, but you can always add more by pressing Ctrl+F8.
            System.out.println("i = " + i);
        }

        int x = 7;
        String s = STR. "int x is \{ x }" ;
        System.out.println("s = " + s);
        s = MYTEMPLATE. "int x is \{ x }" ;
        System.out.println("s = " + s);

        Gatherer<? super String, ?, String> reverser = () ->
                (state, element, downstream) ->
                        downstream.push(new StringBuilder(element).reverse().toString());

        System.out.println(Stream.of("alfa", "beta", "gamma")
                .gather(reverser)
                .gather(withIndex("A")
                        .andThen(withIndex("B"))
                        .andThen(withIndex("C")))
                .collect(Collectors.toList()));
        System.out.println(Stream.of("alfa", "beta", "gamma")
                .gather(reverser)
                .gather(withIndex("A"))
                .gather(withIndex("B"))
                .gather(withIndex("C"))
                .collect(Collectors.toList()));
    }
}


