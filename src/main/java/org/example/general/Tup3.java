package org.example.general;

public record Tup3<A, B, C>(A a, B b, C c) implements Tup {
    public static <A,B,C> Tup3<A, B, C> nTup3(A a, B b, C c) {
        return new Tup3<>(a, b, c);
    }

    @Override
    public int dimension() {
        return 3;
    }
}
