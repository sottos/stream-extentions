package org.example;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.*;

import static java.util.function.Function.identity;

public class GatherEx {

    static <T> Gatherer<T, AtomicInteger, Indexed<T>> withIndex(String name) {
        return new WithIndexGatherer<>(name);
    }

    static class WithIndexGatherer<T> implements Gatherer<T, AtomicInteger, Indexed<T>> {
        final String name;

        public WithIndexGatherer(String name) {
            System.out.println("Creates WithIndex("+name+")" );
            this.name = name;
        }

        @Override
        public Supplier<AtomicInteger> initializer() {
            System.out.println( "Creates Initializer("+name+")" );
            return () -> {
                System.out.println( "Invokes Initializer("+name+")" );
                return new AtomicInteger(0);
            };
        }

        @Override
        public Integrator<AtomicInteger, T, Indexed<T>> integrator() {
            System.out.println( "Creates Integrator("+name+")" );
            return (state, element, downstream) ->
            {
                System.out.println( "Invokes Integrator("+name+")" );
                return downstream.push(new Indexed<>(state.getAndIncrement(), element));
            };
        }

        @Override
        public BiConsumer<AtomicInteger, Downstream<? super Indexed<T>>> finisher() {
            System.out.println( "Creates Finisher("+name+")" );
            return (_, _) -> {
                System.out.println( "Invokes Finisher("+name+")" );
                Gatherer.super.finisher();
            };
        }
    }

    record Indexed<T>(int i, T data) {
    }

    public static void main(String[] args) {
        System.out.println("Using andThen");
        System.out.println(Stream.of(1, 2, 3)
                .gather(withIndex("A")
                        .andThen(withIndex("B"))
                        .andThen(withIndex("C")))
                .collect(Collectors.toList()));
        System.out.println("Using separate gathers");
        System.out.println(Stream.of(1, 2, 3)
                .gather(withIndex("A"))
                .gather(withIndex("B"))
                .gather(withIndex("C"))
                .collect(Collectors.toList()));
        System.out.println("Using separate gathers with map(identity)");
        System.out.println(Stream.of(1, 2, 3)
                .map(identity())
                .gather(withIndex("A"))
                .map(identity())
                .gather(withIndex("B"))
                .map(identity())
                .gather(withIndex("C"))
                .map(identity())
                .collect(Collectors.toList()));


        var withIndx = withIndex("X");
        System.out.println(Stream.of(1, 2, 3)
                .map(identity())
                .gather(withIndx)
                .map(identity())
                .gather(withIndx)
                .map(identity())
                .gather(withIndx)
                .map(identity())
                .collect(Collectors.toList()));

        System.out.println(Stream.of(1, 2, 3)
                .gather(withIndx)
                .gather(withIndx)
                .gather(withIndx)
                .gather(withIndx)
                .gather(withIndx)
                .collect(Collectors.toList()));

        var groupInPair = Gatherers.windowFixed(2);

        System.out.println(Stream.of(1,2,3,4,5,6,7,8,9,10,11)
                .gather(groupInPair)
                .map(identity())
                .gather(groupInPair)
                .map(identity())
                .gather(groupInPair)
                .map(identity())
                .collect(Collectors.toList()));
    }

    private static  Collector<?,?,?> createCollector() {
        System.out.println("Calls Collectors.toList");
        try {
            return Collectors.toList();
        } finally {
            System.out.println("Finished Collectors.toList");
        }
    }
}

