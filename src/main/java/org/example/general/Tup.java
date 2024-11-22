package org.example.general;

public interface Tup {
    default String name() {
        return getClass().getSimpleName();
    }
    int dimension();
}
