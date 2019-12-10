package org.javamaster.b2c.test.multithread;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

/**
 * @author yudong
 * @date 2019/7/2
 */
public class MultiThreadTest {

    /**
     * 初始化4个商店
     */
    List<Shop> shops = Arrays.asList(
            new Shop("BestPrice"),
            new Shop("LetsSaveBig"),
            new Shop("MyFavoriteShop"),
            new Shop("BuyItAll"));


    @Test
    public void test() throws Exception {
        Thread thread1 = new Thread(new Task1());
        thread1.start();

        Task2 thread2 = new Task2();
        thread2.start();

        TimeUnit.SECONDS.sleep(5);
    }


    @Test
    public void test1() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(new Task1());

        Future<String> future4 = executorService.submit(new Task3());
        System.out.println(future4.get());

        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }


    @Test
    public void test2() throws Exception {
        long[] numbers = new long[1_000_000];
        for (int i = 0; i < 1_000_000; i++) {
            numbers[i] = i + 1;
        }
        RecursiveTask<Long> task = new SumRecursiveTask(numbers);
        // 默认创建和当前电脑CPU核心数相同的线程数
        Future<Long> future = ForkJoinPool.commonPool().submit(task);
        System.out.println(future.get());
    }

    @Test
    public void test3() {
        long[] numbers = new long[1_000_000];
        for (int i = 0; i < 1_000_000; i++) {
            numbers[i] = i + 1;
        }
        System.out.println(LongStream.of(numbers).parallel().sum());
    }

    @Test
    public void test4() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 2; i++) {
            executorService.submit(new EvenChecker());
        }
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }


    @Test
    public void test5() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.submit(new Task4());
        }
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    public void test6() {
        long start = System.nanoTime();
        System.out.println(findPricesSingleThread("肥皂", "80552588", shops));
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Invocation sequence after all shop " + invocationTime + " ms");
    }

    public static List<String> findPricesSingleThread(String product, String memberNo, List<Shop> shops) {
        List<String> list = new ArrayList<>(shops.size());
        for (Shop shop : shops) {
            String quoteStr = shop.getPriceQuote(product, memberNo);
            Quote quote = Quote.parse(quoteStr);
            double rate = RateService.getRate("CN", "USA");
            Quote euQuote = new Quote(quote.getShopName(), rate * quote.getPrice(), quote.getDiscountCode());
            String res = Discount.applyDiscount(euQuote);
            list.add(res);
        }
        return list;
    }
}
