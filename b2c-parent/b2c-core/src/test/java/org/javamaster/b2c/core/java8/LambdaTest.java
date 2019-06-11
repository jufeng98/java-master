package org.javamaster.b2c.core.java8;

import org.javamaster.b2c.core.function.ApplePredicate;
import org.javamaster.b2c.core.model.java8.Apple;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class LambdaTest extends CommonCode {

    @Test
    public void testJava7() {
        List<Apple> heavyApples = filterApples(apples, new ApplePredicate() {
            public boolean test(Apple apple) {
                return apple.getWeight() > 150;
            }
        });
        System.out.println(heavyApples);
    }

    @Test
    public void testJava8() {
        List<Apple> heavyApples = filterApples(apples, (Apple apple) -> apple.getWeight() > 150);
        System.out.println(heavyApples);
        // 还可以在简洁一点，省略lambda入参类型，由编译器自行推断
        heavyApples = filterApples(apples, apple -> apple.getWeight() > 150);
        System.out.println(heavyApples);
    }

    /**
     * 根据p对象的test方法表达结果来实现筛选苹果的功能
     */
    public static List<Apple> filterApples(List<Apple> apples, ApplePredicate p) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : apples) {
            if (p.test(apple)) {
                result.add(apple);
            }
        }
        return result;
    }
}
