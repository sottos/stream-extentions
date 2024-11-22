package org.example.general;

public record Tup2<A, B>(A a, B b) implements Tup {
    @Override
    public int dimension() {
        return 2;
    }
}
