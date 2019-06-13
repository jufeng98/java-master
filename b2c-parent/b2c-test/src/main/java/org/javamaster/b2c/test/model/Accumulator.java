package org.javamaster.b2c.test.model;

public class Accumulator {
    public long total = 0;

    public void add(long value) {
        total += value;
    }
}
