package no.sottos.zippers;

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
    final Zippers.ZipWhen zipWhen;

    public MultiZipCollector(C collectorToExtend, Stream<?>[] streams, Functions.ArgsInArrayFunction<JoinedInput> tupCreator, Zippers.ZipWhen zipWhen) {
        this.zipWhen = zipWhen;
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
        return (state, t) -> {

            JoinedInput tup = createTup(state, t);
            if (tup != null) {
                innerAccumulator.accept(state.x, tup);
            }
        };
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

    /**
     * Create a resulting tupl, but if one of the iterators is finished, then return null.
     */
    private JoinedInput createTup(ExtendedState<Accumulator> state, Incoming t) {

        Object[] streamValues = new Object[iterators.length + 1];
        streamValues[0] = t;
        boolean terminated = false;
        for (int i = 0; i < iterators.length; ++i) {
            if (! iterators[i].hasNext()) {
                terminated = true;
                streamValues[i + 1] = null;
            } else {
                streamValues[i + 1] = iterators[i].next();
            }
        }

        return (terminated && zipWhen == Zippers.ZipWhen.WHEN_ALL_HAVE_DATA) ?
                null : tupCreator.apply(streamValues);
    }
}
