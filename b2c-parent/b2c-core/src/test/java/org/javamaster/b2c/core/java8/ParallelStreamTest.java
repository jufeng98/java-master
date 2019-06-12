package org.javamaster.b2c.core.java8;

import org.javamaster.b2c.core.model.java8.Accumulator;
import org.junit.Test;

import java.util.stream.LongStream;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class ParallelStreamTest {

    @Test
    public void test() {
        long total = LongStream.rangeClosed(1, 100).parallel().reduce(0L, Long::sum);
        System.out.println(total);
    }

    @Test
    public void test1() {
        System.out.println(sideEffectSum(10_000_000L));
    }

    public static long sideEffectSum(long n) {
        Accumulator accumulator = new Accumulator();
        // LongStream.rangeClosed(1, n).forEach(accumulator::add);
        LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);
        return accumulator.total;
    }
}
