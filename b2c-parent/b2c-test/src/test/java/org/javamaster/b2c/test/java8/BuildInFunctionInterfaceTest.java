package org.javamaster.b2c.test.java8;

import static java.util.stream.Collectors.toList;
import org.javamaster.b2c.test.model.Apple;
import org.junit.Test;

import java.util.List;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class BuildInFunctionInterfaceTest extends CommonCode {

    @Test
    public void test() {
        int totalWeight = apples.stream()
                // 原始类型的特化接口:ToIntFunction
                .mapToInt(apple -> apple.getWeight())
                .sum();
        System.out.println(totalWeight);
    }

    @Test
    public void test1() {
        List<Integer> heavyApplesColorLength = apples.stream()
                // 函数式接口:Predicate<Apple>
                .filter((Apple apple) -> apple.isGood())
                // 函数式接口:Comparator<Apple>
                .sorted((a1, a2) -> Integer.compare(a1.getWeight(), a2.getWeight()))
                // 函数式接口:Function<Apple, String>
                .map(Apple::getColor)
                // 函数式接口:Function<String, Integer>
                .map(color -> color.length())
                .collect(toList());
        System.out.println(heavyApplesColorLength);
    }

}
