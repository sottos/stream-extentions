package org.example.simple;

public enum ZipWhen {
    /**
     * The zipping will proceed only when all underlying streams can advance
     */
    WHEN_ALL_CAN_ADVANCE,
    /**
     * The zipping will proceed when at least one underlying stream can advance.
     * When one stream is exhausted, emit null element for that stream until the others are exhausted
     */
    WHEN_AT_LEAST_ONE_CAN_ADVANCE
}
