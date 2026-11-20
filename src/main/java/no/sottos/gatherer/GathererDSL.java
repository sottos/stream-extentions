package no.sottos.gatherer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

///
/// Gatherers implementing the same as the corresponding stream intermediate function
///
public final class GathererDSL {

    ///
    /// The stream.filter function as a gatherer
    ///
    public static <T> Gatherer<T, Void, T> filter(Predicate<? super T> p) {
        return Gatherer.of(
                (_, element, downstream) -> {
                    if (p.test(element)) {
                        return downstream.push(element);
                    }
                    return true;
                }
        );
    }

    ///
    /// The stream.map function as a gatherer
    ///
    public static <T, U> Gatherer<T, Void, U> map(Function<? super T, ? extends U> f) {
        return Gatherer.of(
                (_, element, downstream) -> downstream.push(f.apply(element))
        );
    }

    ///
    /// The stream.dropWhile function as a gatherer
    ///
    public static <T> Gatherer<T, AtomicBoolean, T> dropWhile(Predicate<? super T> p) {
        return Gatherer.of(
                () -> new AtomicBoolean(false),
                (passed, element, downstream) -> {
                    if (passed.get()) {
                        return downstream.push(element);
                    } else if (p.test(element)) {
                        return true;
                    }
                    passed.set(true);
                    return downstream.push(element);
                },
                (passedLeft, passedRight) -> {
                    if (passedLeft.get()) {
                        return passedLeft;
                    }
                    if (passedRight.get()) {
                        passedLeft.set(true);
                    }
                    return passedLeft;
                },
                (passed, downstream) -> {
                }
        );
    }

    ///
    /// The stream.takeWhile function as a gatherer
    ///
    public static <T> Gatherer<T, AtomicBoolean, T> takeWhile(Predicate<? super T> p) {
        return Gatherer.of(
                () -> new AtomicBoolean(false),
                (stopped, element, downstream) -> {
                    if (stopped.get()) {
                        return false;
                    } else if (p.test(element)) {
                        return downstream.push(element);
                    }

                    stopped.set(true);
                    return false;
                },
                (stoppedLeft, stoppedRight) -> {
                    if (stoppedLeft.get()) {
                        return stoppedLeft;
                    }

                    if (stoppedRight.get()) {
                        stoppedLeft.set(true);
                    }

                    return stoppedLeft;
                },
                (stopped, downstream) -> {
                }
        );
    }

    ///
    /// The stream.limit function as a gatherer.
    ///
    public static <T> Gatherer<T, AtomicInteger, T> limit(int max) {
        return Gatherer.of(
                () -> new AtomicInteger(0),
                (passed, element, downstream) -> {
                    if (passed.get() >= max) {
                        return false;
                    }
                    passed.incrementAndGet();
                    return downstream.push(element);
                },
                (passedLeft, passedRight) -> {
                    int total = passedLeft.get() + passedRight.get();
                    passedLeft.set(total);
                    return passedLeft;
                },
                (passed, downstream) -> {
                }
        );
    }


    ///
    /// The stream.skip function as a gatherer
    ///
    public static <T> Gatherer<T, AtomicInteger, T> skip(int n) {
        return Gatherer.of(
                () -> new AtomicInteger(0),
                (skipped, element, downstream) -> {
                    if (skipped.get() < n) {
                        skipped.incrementAndGet();
                        return true;
                    }
                    return downstream.push(element);
                },

                // combiner (parallel merge)
                (skippedLeft, skippedRight) -> {
                    int total = skippedLeft.get() + skippedRight.get();
                    skippedLeft.set(total);
                    return skippedLeft;
                },
                (skipped, downstream) -> {
                }
        );
    }

    ///
    /// The stream.flatMap function as a gatherer
    ///
    public static <T, U> Gatherer<T, Void, U> flatMap(
            Function<? super T, ? extends Stream<? extends U>> flatMapper) {

        return Gatherer.of(
                (_, element, downstream) -> {
                    try (Stream<? extends U> s = flatMapper.apply(element)) {
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

    ///
    /// A gatherer only pushing elements downstream; first sends the element to the consumer
    ///
    /// The consumer may be used for tracing and alike.
    ///
    public static <T> Gatherer<T, Void, T> peek(Consumer<? super T> consumer) {
        return Gatherer.of(
                (_, element, downstream) -> {
                    consumer.accept(element);
                    return downstream.push(element);
                }
        );
    }

    ///
    /// A gatherer doing nothing, just passes upstream into downstream, useful to represent an empty pipeline
    ///
    public static <T> Gatherer<T, Void, T> identity() {
        return Gatherer.of(
                (_, element, downstream) -> downstream.push(element)
        );
    }

    ///
    /// Builds a gatherer which is a combination of other gatherers, a pipeline that can be used as a variable.
    ///
    /// The inbetween gatherers does not have to be stateless, since the gatherer chain is created anew each time
    /// the toGatherer is called.
    ///
    /// Remark that stateful elements should be created anew, so a Pipeline with a filter element
    /// where the predicate itself is stateful and created outside the pipeline will not be safe to reuse.
    ///
    /// - Generation
    ///    - Gatherer<X,Y> p = Pipeline.<>start().map(...).filter(...).peek(debugLog).map(...).build;
    /// - Use
    ///    - stream.gather(p)....
    ///
    /// Since all functions like filter,map, flatMap ... returns a gatherer.andThen, the <IN> type never changes.
    /// The <OUT> type may change between calls.
    ///
    /// The pipeLine is the complete transformation from IN to OUT independent on how many steps there are.
    ///
    /// @param <IN>  The upstream element type
    /// @param <OUT> The type of the output element type

    public static final class Pipeline<IN, OUT> {

        private final Supplier<Gatherer<IN, ?, OUT>> thisGatherer;

        private Pipeline(Supplier<Gatherer<IN, ?, OUT>> p) {
            this.thisGatherer = p;
        }

        // Start: identity-pipeline IN -> IN
        public static <IN> Pipeline<IN, IN> start() {
            return new Pipeline<>(GathererDSL::identity);
        }

        // filter: keeps the OUT-type <IN, OUT> -> <IN, OUT>
        public Pipeline<IN, OUT> filter(Predicate<? super OUT> predicate) {
            return new Pipeline<>(() -> thisGatherer.get().andThen(GathererDSL.filter(predicate)));
        }

        // map: changes OUT-type <IN, OUT> -> <IN, U>
        public <U> Pipeline<IN, U> map(Function<? super OUT, ? extends U> f) {
            return new Pipeline<>(() -> thisGatherer.get().andThen(GathererDSL.map(f)));
        }

        // flatMap: changes the OUT-type to U
        public <U> Pipeline<IN, U> flatMap(Function<? super OUT, ? extends Stream<? extends U>> f) {
            return new Pipeline<>(() -> thisGatherer.get().andThen(GathererDSL.flatMap(f)));
        }

        // peek: keeps the OUT-type
        public Pipeline<IN, OUT> peek(Consumer<? super OUT> consumer) {
            return new Pipeline<>(() -> thisGatherer.get().andThen(GathererDSL.peek(consumer)));
        }

        // gather: changes the OUT-type to U
        public <U> Pipeline<IN, U> gather(Supplier<? extends Gatherer<OUT, ?, U>> gatherer) {
            return new Pipeline<>(() -> thisGatherer.get().andThen(gatherer.get()));
        }

        // finished pipeline or just access the inner pipeLine: IN -> OUT
        public Gatherer<IN, ?, OUT> toGatherer() {
            return thisGatherer.get();
        }

        // Chaining two pipelines
        public <U> Pipeline<IN, U> then(Pipeline<OUT, U> next) {
            return new Pipeline<>(() -> this.toGatherer().andThen(next.toGatherer()));
        }
    }
}