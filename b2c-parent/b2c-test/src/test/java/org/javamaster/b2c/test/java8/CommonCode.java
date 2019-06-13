package org.javamaster.b2c.test.java8;

import org.javamaster.b2c.test.model.Apple;
import org.javamaster.b2c.test.model.Dish;
import org.javamaster.b2c.test.model.Transaction;
import org.junit.BeforeClass;

import java.util.Arrays;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class CommonCode {
    protected static List<Apple> apples;
    protected static List<Dish> menus;
    protected static List<Transaction> transactions;

    @BeforeClass
    public static void init() {
        apples = Arrays.asList(
                new Apple("green", 300, true),
                new Apple("yellow", 800, false),
                new Apple("red", 100, false),
                new Apple("yellow", 500, true)
        );

        menus = Arrays.asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH));

        transactions = Arrays.asList(
                new Transaction(null, 2018, 5, 1200, "AR"),
                new Transaction(null, 2018, 8, 1800, "AR"),
                new Transaction(null, 2019, 5, 2900, "AR"),
                new Transaction(null, 2018, 7, 1800, "AR"),
                new Transaction(null, 2018, 7, 8300, "CH"),
                new Transaction(null, 2019, 7, 8900, "CH"),
                new Transaction(null, 2019, 7, 1000, "CH")
        );
    }
}
