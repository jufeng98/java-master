package org.javamaster.b2c.core.model.java8;

public class Accumulator {
    public long total = 0;

    public void add(long value) {
        total += value;
    }
}
