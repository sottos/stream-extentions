package no.sottos.strdsl;

import java.util.function.*;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public final class GathererDSL {

    // -----------------------------
    //  Primitive gatherers
    // -----------------------------

    public static <T> Gatherer<T, Void, T> filter(Predicate<? super T> p) {
        return Gatherer.of(
                (state, element,  downstream) -> {
                    if (p.test(element)) {
                        return downstream.push(element);
                    }
                    return true;
                }
        );
    }

    public static <T, U> Gatherer<T, Void, U> map(Function<? super T, ? extends U> f) {
        return Gatherer.of(
                (state, element, downstream) -> downstream.push(f.apply(element))
        );
    }

    public static <T, U> Gatherer<T, Void, U> flatMap(
            Function<? super T, ? extends Stream<? extends U>> mapper) {

        return Gatherer.of(
                (state, element, downstream) -> {
                    try (Stream<? extends U> s = mapper.apply(element)) {
                        var it = s.iterator();
                        while (it.hasNext()) {
                            if (!downstream.push(it.next())) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
        );
    }

    public static <T> Gatherer<T, Void, T> peek(Consumer<? super T> c) {
        return Gatherer.of(
                (state, element, downstream) -> {
                    c.accept(element);
                    return downstream.push(element);
                }
        );
    }
    public static <T> Gatherer<T, Void, T> identity() {
        return Gatherer.of(
                (state, element, downstream) -> downstream.push(element)
        );
    }

    // -----------------------------
    //  DSL builder
    // -----------------------------
    // IN  = input-type til hele pipelinen
    // OUT = output-type etter siste operasjon

    public static final class G<IN, OUT> {

        private final Gatherer<IN, ?, OUT> g;

        private G(Gatherer<IN, ?, OUT> g) {
            this.g = g;
        }

        // Start: identity-pipeline T -> T
        public static <T> G<T, T> start() {
            return new G<>(GathererDSL.identity());
        }

        // filter: beholder OUT-typen
        // G<IN, OUT> -> G<IN, OUT>, men filter-kriteriet er på OUT
        public G<IN, OUT> filter(Predicate<? super OUT> p) {
            Gatherer<IN, ?, OUT> next = g.andThen(GathererDSL.filter(p));
            return new G<>(next);
        }

        // map: endrer OUT-typen
        // G<IN, OUT> -> G<IN, U>
        public <U> G<IN, U> map(Function<? super OUT, ? extends U> f) {
            Gatherer<IN, ?, U> next = g.andThen(GathererDSL.map(f));
            return new G<>(next);
        }

        // flatMap: endrer OUT-typen til U, og kan gi 0..n U per OUT
        public <U> G<IN, U> flatMap(Function<? super OUT, ? extends Stream<? extends U>> f) {
            Gatherer<IN, ?, U> next = g.andThen(GathererDSL.flatMap(f));
            return new G<>(next);
        }

        // peek: beholder OUT-typen
        public G<IN, OUT> peek(Consumer<? super OUT> c) {
            Gatherer<IN, ?, OUT> next = g.andThen(GathererDSL.peek(c));
            return new G<>(next);
        }

        // Ferdig pipeline: IN -> OUT
        public Gatherer<IN, ?, OUT> build() {
            return g;
        }
    }

}