package org.example;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static java.lang.StringTemplate.STR;

public class GatherTestWhereDownStreamPushResultIsNotRespected {

    public static void main(String[] args) {
        Optional<String> f1 = Stream.of("1234")
                .gather(logElement("splitText", splitText()))
                .gather(logElement("GatherDoNothingReturnTrue", doNothingReturnTrue()))
                .findFirst();
        System.out.println(f1 +"\n");

        Optional<String> ff1 = Stream.of("1234")
                .flatMap(s -> s.chars().mapToObj(Character::toString))
                .gather(logElement("GatherDoNothingReturnTrue", doNothingReturnTrue()))
                .findFirst();
        System.out.println(ff1 +"\n");

        Optional<String> f2 = Stream.of("efgh")
                .gather(logElement("splitText", splitText()))
                .gather(logElement("GatherDoNothingReturnTrue", doNothingReturnTrue()))
                .peek(logElement("PeekDoNothing", s -> {}))
                .findFirst();
        System.out.println(f2+"\n");

        Optional<String> f3 = Stream.of("EFGH")
                .gather(logElement("splitText", splitText()))
                .gather(logElement("GatherDoNothingReturnTrue", doNothingReturnTrue()))
                .gather(logElement("GatherDoNothing", doNothing()))
                .findFirst();
        System.out.println(f3+"\n");
    }

    public static <T> Gatherer<T, ?, T> doNothing() {
        // This is correct impl of doNothing
        return Gatherer.of((_, element, downstream) -> downstream.push(element));
    }
    public static <T> Gatherer<T, ?, T> doNothingReturnTrue() {
        // This is erronous since it does not respect the downstream.push's return value
        return Gatherer.of((_, element, downstream) -> { downstream.push(element); return true; });
    }
    public static Gatherer<String, ?, String> splitText() {
        Gatherer.Integrator<Void, String, String> integrator = (_, s, downstream) -> {
            Iterator<String> iterator = s.chars().mapToObj(Character::toString).iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (!downstream.push(next)) {
                    return false;
                }
            }
            return true;
        };
        return Gatherer.of(integrator);
    }



    private static void println(String name, Object s) {
        System.out.println(STR. "\{ name }('\{ s }')" );
    }

    private static <T> Consumer<T> logElement(String name, Consumer<? super T> c) {
        return x -> {
            println(name, x);
            c.accept(x);
        };
    }

    private static <T, A, R> Gatherer<T, A, R> logElement(String name, Gatherer<T, A, R> gatherer) {
        return new Gatherer<T, A, R>() {
            // Still only logs in integrator
            @Override
            public Integrator<A, T, R> integrator() {
                return (state, elem, downstream) -> {
                    println(name, elem);
                    return gatherer.integrator().integrate(state, elem, downstream);
                };
            }
        };
    }
}

