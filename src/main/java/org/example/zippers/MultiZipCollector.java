package org.example.zippers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public class MultiZipCollector
        <Incoming, JoinedInput, Result, Accumulator, C extends Collector<JoinedInput, Accumulator, Result>>
        implements Collector<Incoming, MultiZipCollector.ExtendedState<Accumulator>, Result> {

    public record ExtendedState<Accumulator>(Accumulator x, Iterator<?>[] iterators) {
    }

    final Set<Collector.Characteristics> characteristics;
    final C collectorToExtend;
    final Iterator<?>[] iterators;
    final Functions.ArgsInArrayFunction<JoinedInput> tupCreator;

    public MultiZipCollector(C collectorToExtend, Stream<?>[] streams, Functions.ArgsInArrayFunction<JoinedInput> tupCreator) {
        this.collectorToExtend = collectorToExtend;
        var copy = new HashSet<>(collectorToExtend.characteristics());
        copy.remove(CONCURRENT);
        copy.remove(IDENTITY_FINISH);
        this.characteristics = copy;

        this.iterators = new Iterator<?>[streams.length];
        for (int i = 0; i < this.iterators.length; ++i) {
            this.iterators[i] = streams[i].iterator();
        }
        this.tupCreator = tupCreator;

    }

    @Override
    public Supplier<ExtendedState<Accumulator>> supplier() {
        return () -> new ExtendedState<>(collectorToExtend.supplier().get(), iterators);
    }

    @Override
    public BiConsumer<ExtendedState<Accumulator>, Incoming> accumulator() {
        BiConsumer<Accumulator, JoinedInput> innerAccumulator = collectorToExtend.accumulator();
        return (state, t) -> innerAccumulator.accept(state.x, createTup(state, t));
    }

    @Override
    public BinaryOperator<ExtendedState<Accumulator>> combiner() {
        return (s1, s2) -> {
            throw new IllegalStateException("This is not a concurrent stream collector");
        };
    }

    @Override
    public Function<ExtendedState<Accumulator>, Result> finisher() {
        Function<Accumulator, Result> innerFinisher = collectorToExtend.finisher();
        return s -> innerFinisher.apply(s.x());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }

    private JoinedInput createTup(ExtendedState<Accumulator> state, Incoming t) {

        Object[] streamValues = new Object[iterators.length + 1];
        streamValues[0] = t;
        for (int i = 0; i < iterators.length; ++i) {
            streamValues[i + 1] = iterators[i].hasNext() ? iterators[i].next() : null;
        }
        return tupCreator.apply(streamValues);
    }
}
