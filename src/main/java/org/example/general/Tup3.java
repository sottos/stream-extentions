package org.example.general;

public record Tup3<A, B, C>(A a, B b, C c) implements Tup {
    @Override
    public int dimension() {
        return 3;
    }
}
