package org.example.general;

public record TupN<T>(T... ts) implements Tup {

    @SafeVarargs
    public TupN {
    }

    @Override
    public int dimension() { return ts.length; }
}
