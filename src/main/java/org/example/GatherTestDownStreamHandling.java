package org.example;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatherTestDownStreamHandling {

    public static void main(String[] args) {
        AtomicInteger countWhenUsingGathererFlatMap = new AtomicInteger();
        AtomicInteger countWhenUsingStreamFlatMap = new AtomicInteger();

        Stream.of("1234")
                .gather(flatMapGatherer(s -> s.chars().mapToObj(Character::toString)))
                .gather(counterGatherer(countWhenUsingGathererFlatMap))
                .findFirst();

        Stream.of("1234")
                .flatMap(s -> s.chars().mapToObj(Character::toString))
                .gather(counterGatherer(countWhenUsingStreamFlatMap))
                .findFirst();

        assertEquals(1, countWhenUsingStreamFlatMap.get(), "findFirst should find the first element passed to it");
        assertEquals(countWhenUsingStreamFlatMap.get(), countWhenUsingGathererFlatMap.get(),
                "Number of elements passed from the flatMapGatherer and Stream.flatMap should be the same");
    }


    public static <T, U> Gatherer<T, ?, U> flatMapGatherer(
            Function<? super T, Stream<? extends U>> mapper) {
        Gatherer.Integrator<Void, T, U> integrator = (_, element, downstream) -> {
            Iterator<? extends U> iterator = mapper.apply(element).iterator();
            while (iterator.hasNext()) {
                if (!downstream.push(iterator.next())) {
                    return false;
                }
            }
            return true;
        };
        return Gatherer.of(integrator);
    }

    /**
     * Gatherer counting number of elements passed through it.
     * Integrator passes element to downstream and increments given AtomicInteger for each.
     */
    public static <T> Gatherer<T, ?, T> counterGatherer(AtomicInteger passthroughCounter) {
        // This implementation does not respect the downstream.push's return value
        return Gatherer.of((_, element, downstream) -> {
            passthroughCounter.incrementAndGet();
            if (downstream.isRejecting()) {
                System.out.println("isRejecting = true");
            }
            downstream.push(element); // TODO: correct is return downstream.push(element)
            return true;
        });
    }
}

