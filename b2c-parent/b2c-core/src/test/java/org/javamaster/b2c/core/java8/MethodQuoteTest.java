package org.javamaster.b2c.core.java8;

import org.javamaster.b2c.core.model.java8.Apple;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class MethodQuoteTest extends CommonCode {

    @Test
    public void testMethodQuote() {
        apples.stream().map(apple -> apple.getWeight()).forEach(weight -> System.out.println(weight));
        apples.stream().map(Apple::getWeight).forEach(System.out::println);
    }

    @Test
    public void testConstructMethodQuote() {
        Supplier<Apple> c1 = () -> new Apple();
        Apple a1 = c1.get();
        System.out.println(a1);

        Supplier<Apple> c2 = Apple::new;
        Apple a2 = c2.get();
        System.out.println(a2);

        BiFunction<String, Integer, Apple> c3 = Apple::new;
        Apple a3 = c3.apply("green", 110);
        System.out.println(a3);
    }


}
