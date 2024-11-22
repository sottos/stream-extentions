package org.example.general;

public record Tup4<A, B, C, D>(A a, B b, C c, D d) implements Tup {
    @Override
    public int dimension() {
        return 4;
    }
}
