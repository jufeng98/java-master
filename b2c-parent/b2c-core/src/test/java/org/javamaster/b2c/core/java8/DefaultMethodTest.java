package org.javamaster.b2c.core.java8;

import static java.util.stream.Collectors.toList;
import org.javamaster.b2c.core.model.java8.Apple;
import org.junit.Test;

import java.util.List;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class DefaultMethodTest extends CommonCode {

    @Test
    public void test() {
        List<Apple> heavyApples = apples.stream()
                .filter((Apple a) -> a.getWeight() > 150)
                .collect(toList());
        System.out.println(heavyApples);
    }
}
