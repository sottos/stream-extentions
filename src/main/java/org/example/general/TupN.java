package org.example.general;

public record TupN<T>(T... ts) implements Tup {
    @Override
    public int dimension() { return ts.length; }
}
