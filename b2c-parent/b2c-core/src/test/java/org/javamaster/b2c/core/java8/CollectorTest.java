package org.javamaster.b2c.core.java8;

import org.javamaster.b2c.core.model.java8.Dish;
import org.javamaster.b2c.core.model.java8.Transaction;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.partitioningBy;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class CollectorTest extends CommonCode {

    @Test
    public void test() {
        // 假设你想要找出菜单中热量最高的菜。你可以使用两个收集器，Collectors.maxBy和Collectors.minBy，来计算流中的最大或最小值。
        Comparator<Dish> dishCaloriesComparator = comparingInt(Dish::getCalories);
        Optional<Dish> mostCalorieDish = menus.stream().collect(maxBy(dishCaloriesComparator));
        System.out.println(mostCalorieDish);
        // 汇总
        int totalCalories = menus.stream().collect(Collectors.summingInt(Dish::getCalories));
        System.out.println(totalCalories);
        // 求平均数
        double avgCalories = menus.stream().collect(Collectors.averagingInt(Dish::getCalories));
        System.out.println(avgCalories);
        // 连接字符串
        String shortmenus = menus.stream().map(Dish::getName).collect(Collectors.joining(", "));
        System.out.println(shortmenus);
        // 分组，根据交易的货币类型进行分组:
        Map<String, List<Transaction>> transactionsByCurrencies = transactions.stream()
                .collect(groupingBy(Transaction::getCurrency));
        System.out.println(transactionsByCurrencies);
        // 查找每个子组中热量最高的Dish
        Map<Dish.Type, Dish> mostCaloricByType = menus.stream()
                .collect(groupingBy(Dish::getType, collectingAndThen(maxBy(comparingInt(Dish::getCalories)), Optional::get)));
        System.out.println(mostCaloricByType);
        // 想要把菜单按照素食和非素食分开
        Map<Boolean, List<Dish>> partitionedMenu = menus.stream().collect(partitioningBy(Dish::isVegetarian));
        System.out.println(partitionedMenu);
    }
}
