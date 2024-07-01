package org.example;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class GatherTestFinisher {

    public static void main(String[] args) {
        var x = Stream.of("4321", "35")
                .mapMulti((s, consumer) -> s.chars().mapToObj(Character::toString).forEach(
                        c -> {
                            System.out.println("c = " + c);
                            consumer.accept(c);
                        }
                )).findFirst();
        System.out.println("x = " + x);

        Optional<String> first = Stream.of("4321abcd339")
                .flatMap(s -> s.chars().mapToObj(Character::toString))
                .gather(numbersSortedCharactersPassedOn())
                .findFirst();
        System.out.println("first = " + first);

    }


    public static Gatherer<String, ArrayList<String>, String> numbersSortedCharactersPassedOn(
    ) {
        Supplier<ArrayList<String>> initializer = ArrayList::new;
        Gatherer.Integrator<ArrayList<String>, String, String> integrator = (state, element, downstream) -> {
            if (element != null && !element.isEmpty() && Character.isDigit(element.charAt(0))) {
                state.add(element);
                System.out.println("adds to state = " + element);
                return true;
            } else {
                System.out.println("pushes element = " + element);
                return downstream.push(element);
            }
        };
        BiConsumer<ArrayList<String>, Gatherer.Downstream<? super String>> arrayListDownstreamBiConsumer = (state, downStream) -> {
            if (downStream.isRejecting()) {
                System.out.println("Finisher returns doing nothing");
                return;
            }
            state.sort(CharSequence::compare);
            System.out.println("State sorted, length is " + state.size());
            for (int i = 0; i < state.size(); ++i) {
                System.out.println("pushes element = " + state.get(i));
                if (!downStream.push(state.get(i))) {
                    System.out.println("Returns after pushing");
                    return;
                }
            }
            System.out.println("Returns after for loop in finisher");
        };

        return Gatherer.ofSequential(
                initializer,
                integrator,
                arrayListDownstreamBiConsumer);
    }

}

