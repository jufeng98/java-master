package org.javamaster.b2c.core.java8;

import org.javamaster.b2c.core.model.java8.Dish;
import org.javamaster.b2c.core.model.java8.Transaction;
import org.javamaster.b2c.core.model.java8.TransactionVo;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class StreamTest extends CommonCode {

    @Test
    public void testJava7() {
        List<Transaction> tmpTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getValue() > 1500) {
                tmpTransactions.add(transaction);
            }
        }
        Collections.sort(tmpTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction o1, Transaction o2) {
                int i = Integer.compare(o2.getYear(), o1.getYear());
                if (i != 0) {
                    return i;
                }
                return Integer.compare(o1.getMonth(), o2.getMonth());
            }
        });
        List<TransactionVo> tmpTransactionVos = new ArrayList<>(tmpTransactions.size());
        for (Transaction tmpTransaction : tmpTransactions) {
            TransactionVo transactionVo = new TransactionVo();
            BeanUtils.copyProperties(tmpTransaction, transactionVo);
            tmpTransactionVos.add(transactionVo);
        }
        Map<String, List<TransactionVo>> map = new HashMap<>();
        for (TransactionVo tmpTransactionVo : tmpTransactionVos) {
            List<TransactionVo> list = map.get(tmpTransactionVo.getCurrency());
            if (list == null) {
                list = new ArrayList<>();
                map.put(tmpTransactionVo.getCurrency(), list);
            }
            list.add(tmpTransactionVo);
        }
        System.out.println(map);
    }

    @Test
    public void testJava8() {
        Map<String, List<TransactionVo>> map = transactions.stream()
                // 接受Predicate,过滤掉所有返回false的元素
                .filter(transaction -> transaction.getValue() > 1500)
                // 接受Comparator，对元素进行排序
                .sorted(Comparator.comparing(Transaction::getYear).reversed().thenComparing(Transaction::getMonth))
                // 接受Function，将元素转换为TransactionVo
                .map(transaction -> {
                    TransactionVo transactionVo = new TransactionVo();
                    BeanUtils.copyProperties(transaction, transactionVo);
                    return transactionVo;
                })
                // 接受Collector,将元素按给定的规则进行分组
                .collect(groupingBy(TransactionVo::getCurrency));
        System.out.println(map);
    }

    @Test
    public void test() {
        List<String> names = menus.stream()
                // 接受Predicate,过滤掉所有返回false的元素
                .filter(d -> d.getCalories() > 300)
                //  接受Function,将元素转化为String
                .map(Dish::getName)
                // 只取前三个元素
                .limit(3)
                // 接受Collector,将元素规约成一个List
                .collect(toList());
        System.out.println(names);
    }

    @Test
    public void test1() {
        List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
        numbers = numbers.stream().filter(i -> i % 2 == 0).distinct().collect(Collectors.toList());
        System.out.println(numbers);
    }

    @Test
    public void test2() {
        List<Dish> dishes = menus.stream().filter(d -> d.getCalories() > 300).limit(3).collect(toList());
        System.out.println(dishes);
        dishes = menus.stream().filter(d -> d.getCalories() > 300).skip(2).collect(toList());
        System.out.println(dishes);
        if (menus.stream().anyMatch(Dish::isVegetarian)) {
            System.out.println("The menu is (somewhat) vegetarian friendly!!");
        }
        boolean isHealthy = menus.stream().allMatch(d -> d.getCalories() < 1000);
        System.out.println(isHealthy);
        isHealthy = menus.stream().noneMatch(d -> d.getCalories() >= 1000);
        System.out.println(isHealthy);
    }

    @Test
    public void test3() {
        List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
        // 元素求和
        int sum = numbers.stream().reduce(0, (a, b) -> a * b);
        System.out.println(sum);
        // 可以使用方法引用让这段代码更简洁。在Java 8中，Integer类现在有了一个静态的sum方法来对两个数求和
        sum = numbers.stream().reduce(0, Integer::sum);
        System.out.println(sum);
        // 求最大值和最小值
        Optional<Integer> max = numbers.stream().reduce(Integer::max);
        System.out.println(max);
        Optional<Integer> min = numbers.stream().reduce(Integer::min);
        System.out.println(min);
    }

    @Test
    public void test4() {
        // 这段代码的问题是，它有一个暗含的装箱成本。每个Integer都必须拆箱成一个原始类型，再进行求和。
        int calories = menus.stream().map(Dish::getCalories).reduce(0, Integer::sum);
        System.out.println(calories);
        // Java 8引入了三个原始类型特化流接口来解决这个问题：IntStream、DoubleStream和LongStream，分别
        // 将流中的元素特化为int、long和double，从而避免了暗含的装箱成本。
        calories = menus.stream().mapToInt(Dish::getCalories).sum();
        System.out.println(calories);
        // 一个从1到100的偶数流
        IntStream evenNumbers = IntStream.rangeClosed(1, 100).filter(n -> n % 2 == 0);
        System.out.println(evenNumbers.average());
    }

    @Test
    public void test5() throws Exception {
        // 从静态工厂方法创建流
        Stream<String> stream = Stream.of("Java 8 ", "Lambdas ", "In ", "Action");
        stream.map(String::toUpperCase).forEach(System.out::println);
        int[] numbers = {2, 3, 5, 7, 11, 13};
        // 从数组创建流
        int sum = Arrays.stream(numbers).sum();
        System.out.println(sum);
        File file = ResourceUtils.getFile("classpath:application.yml");
        // 从文件创建流
        Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset());
        long uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(" "))).distinct().count();
        System.out.println(uniqueWords);
        // 从iterate方法创建无限流
        Stream.iterate(0, n -> n + 2).limit(10).forEach(System.out::println);
        // 从generate方法创建无限流
        Stream.generate(Math::random).limit(5).forEach(System.out::println);
    }
}
