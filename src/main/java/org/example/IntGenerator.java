package org.example;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IntGenerator {
    public static Stream<Integer> generate(int start, UnaryOperator<Integer> f) {
        return StreamSupport.stream(new IntSpliterator(start, f), false);
    }

    private static class IntSpliterator implements Spliterator<Integer> {

        int currentValue;
        UnaryOperator<Integer> f;

        public IntSpliterator(int startValue, UnaryOperator<Integer> f) {
            this.currentValue = startValue;
            this.f = f;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Integer> action) {
            try {
                action.accept(currentValue);
            } finally {
                currentValue = f.apply(currentValue);
            }
            return true;
        }

        @Override
        public IntSpliterator trySplit() {
            // Cannot split
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL | IMMUTABLE | DISTINCT;
        }
    }
}
