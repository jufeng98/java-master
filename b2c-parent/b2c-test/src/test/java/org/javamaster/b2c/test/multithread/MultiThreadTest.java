package org.javamaster.b2c.test.multithread;

import static java.util.stream.Collectors.toList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
    public void test6() throws Exception {
        Queue<String> cachePoolQueue = new LinkedList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(() -> {
            Producer producer = new Producer(cachePoolQueue);
            while (true) {
                producer.produce();
            }
        });
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                Consumer consumer = new Consumer(cachePoolQueue);
                while (true) {
                    consumer.consume();
                }
            });
        }
        executorService.shutdown();
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void test7() throws Exception {
        Queue<String> cachePoolQueue = new LinkedList<>();
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(() -> {
            Producer2 producer = new Producer2(cachePoolQueue, reentrantLock, condition);
            while (true) {
                producer.produce();
            }
        });
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                Consumer2 consumer = new Consumer2(cachePoolQueue, reentrantLock, condition);
                while (true) {
                    consumer.consume();
                }
            });
        }
        executorService.shutdown();
        TimeUnit.SECONDS.sleep(5);
    }


    @Test
    public void test8() throws Exception {
        BlockingQueue<String> cachePoolQueue = new ArrayBlockingQueue<>(1);
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(() -> {
            Producer3 producer = new Producer3(cachePoolQueue);
            while (true) {
                producer.produce();
            }
        });
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                Consumer3 consumer = new Consumer3(cachePoolQueue);
                while (true) {
                    consumer.consume();
                }
            });
        }
        executorService.shutdown();
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void test9() throws Exception {
        // 裁判的发令枪只响一次
        CountDownLatch startCountDownLatch = new CountDownLatch(1);
        int runnerSize = 10;
        // 运动员有多个
        CountDownLatch endCountDownLatch = new CountDownLatch(runnerSize);
        ExecutorService executorService = Executors.newCachedThreadPool();

        Judge judge = new Judge(startCountDownLatch, endCountDownLatch);
        Future<Integer> averageFuture = executorService.submit(judge);

        for (int i = 0; i < runnerSize; i++) {
            Future<Integer> scoreFuture = executorService.submit(new Runner(startCountDownLatch, endCountDownLatch));
            judge.collectScore(scoreFuture);
        }
        executorService.shutdown();
        System.out.println("运动员平均耗时:" + averageFuture.get() + "秒");
    }

    @Test
    public void test10() throws Exception {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2, () -> {
            // 这里是栅栏动作,当栅栏被撤销(即两个队伍的挖掘任务都已完成),这里就会被执行
            System.out.println("恭喜,隧道已经打通!");
        });
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new LeftDigging(cyclicBarrier));
        executorService.submit(new RightDigging(cyclicBarrier));
        executorService.shutdown();

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test11() throws Exception {
        Exchanger<List<String>> exchanger = new Exchanger<>();
        List<String> producerList = new CopyOnWriteArrayList<>();
        List<String> consumerList = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(() -> {
            ProducerExchanger producerExchanger = new ProducerExchanger(exchanger, producerList);
            while (true) {
                producerExchanger.produce();
            }
        });
        executorService.submit(() -> {
            ConsumerExchanger consumerExchanger = new ConsumerExchanger(exchanger, consumerList);
            while (true) {
                consumerExchanger.consumer();
            }
        });
        executorService.shutdown();
        TimeUnit.SECONDS.sleep(1000);
    }

    @Test
    public void test12() {
        long start = System.nanoTime();
        List<Double> prices = findPrices("肥皂", shops);
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("同步状态下查询价格耗时" + invocationTime + " ms," + "价格列表:" + prices);
    }

    public static List<Double> findPrices(String product, List<Shop> shops) {
        return shops.stream().map(shop -> shop.getPrice(product)).collect(toList());
    }

    @Test
    public void test13() {
        long start = System.nanoTime();
        List<Future<Double>> pricesFuture = findPricesAsync("肥皂", shops);
        // 这里可以做其他事情
        doSomethingElse();
        List<Double> prices = pricesFuture.stream()
                .map(doubleFuture -> {
                    try {
                        return doubleFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toList());
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("异步状态下查询价格耗时" + invocationTime + " ms," + "价格列表:" + prices);
    }

    public static List<Future<Double>> findPricesAsync(String product, List<Shop> shops) {
        return shops.stream().map(shop -> shop.getPriceAsync(product)).collect(toList());
    }

    private static void doSomethingElse() {
        // 其他任务...
    }

    @Test
    public void test14() {
        long start = System.nanoTime();
        List<Double> prices = shops.parallelStream().map(shop -> shop.getPrice("肥皂")).collect(toList());
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("并行流方式查询价格耗时" + invocationTime + " ms," + "价格列表:" + prices);
    }

    @Test
    public void test15() {
        long start = System.nanoTime();
        List<CompletableFuture<Double>> priceFutures = shops.stream()
                // 使用CompletableFuture以异步方式计算每种商品的价格
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice("肥皂")))
                .collect(toList());
        // 等待所有异步操作结束
        List<Double> prices = priceFutures.stream().map(CompletableFuture::join).collect(toList());
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("CompletableFuture异步方式查询价格耗时" + invocationTime + " ms," + "价格列表:" + prices);
    }

    @Test
    public void test16() {
        long start = System.nanoTime();
        List<Double> discountPrices = findDiscountPrices("肥皂", "100001", shops);
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("折扣服务同步方式查询价格耗时" + invocationTime + " ms," + "价格列表:" + discountPrices);
    }

    public static List<Double> findDiscountPrices(String product, String memberNo, List<Shop> shops) {
        return shops.stream()
                .map(shop -> shop.getPriceQuote(product, memberNo))
                .map(Quote::parse)
                .map(Discount::applyDiscount)
                .collect(toList());
    }

    @Test
    public void test17() {
        long start = System.nanoTime();
        List<Double> discountPrices = findDiscountPricesAsync("肥皂", "100001", shops);
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("折扣服务异步方式查询价格耗时" + invocationTime + " ms," + "价格列表:" + discountPrices);
    }

    public List<Double> findDiscountPricesAsync(String product, String memberNo, List<Shop> shops) {

        List<CompletableFuture<Double>> priceFutures = shops.stream()
                // 以异步方式取得每个shop中指定产品的原始价格
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPriceQuote(product, memberNo)))
                // Quote对象存在时，对其返回的值进行转换
                .map(future -> future.thenApply(Quote::parse))
                // 使用另一个异步任务构造期望的Future，申请折扣
                .map(future -> future.thenCompose(quote -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote))))
                .collect(toList());

        // 等待流中的所有Future执行完毕，并提取各自的返回值
        return priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(toList());
    }


    @Test
    public void test18() {
        long start = System.nanoTime();
        List<CompletableFuture<Double>> futures = shops.stream()
                .map(shop ->
                        CompletableFuture
                                // 查商品价格操作和查兑换汇率操作同时进行,当两者都完成时将结果进行整合
                                .supplyAsync(() -> shop.getPrice("肥皂"))
                                .thenCombine(CompletableFuture.supplyAsync(() -> RateService.getRate("RMB", "USD")), (price, rate) -> price * rate)
                )
                .collect(toList());
        List<Double> usdPrices = futures.stream()
                .map(CompletableFuture::join)
                .collect(toList());
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("兑换汇率服务异步方式查询价格耗时" + invocationTime + " ms," + "美元价格列表:" + usdPrices);
    }

    ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void test19() throws ExecutionException, InterruptedException {
        long start = System.nanoTime();
        List<Future<Double>> usdFuturePrices = new ArrayList<>(shops.size());
        for (Shop shop : shops) {
            // 创建一个查询人民币到美元转换汇率的Future
            final Future<Double> usdFutureRate = executor.submit(new Callable<Double>() {
                public Double call() {
                    return RateService.getRate("RMB", "USD");
                }
            });
            // 在第二个Future中查询指定商店中特定商品的价格
            Future<Double> usdFuturePrice = executor.submit(new Callable<Double>() {
                public Double call() throws ExecutionException, InterruptedException {
                    double rmbPrice = shop.getPrice("肥皂");
                    // 在查找价格操作的同一个Future中， 将价格和汇率做乘法计算出汇后价格
                    return rmbPrice * usdFutureRate.get();
                }
            });
            usdFuturePrices.add(usdFuturePrice);
        }
        List<Double> usdPrices = new ArrayList<>(usdFuturePrices.size());
        for (Future<Double> usdFuturePrice : usdFuturePrices) {
            usdPrices.add(usdFuturePrice.get());
        }
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Java7方式兑换汇率服务异步方式查询价格耗时" + invocationTime + " ms," + "美元价格列表:" + usdPrices);
    }

    @Test
    public void test20() throws InterruptedException {
        findPricesStream("肥皂", "100001", shops)
                .map(f -> f.thenAccept(System.out::println));

        // 防止主线程结束导致应用退出
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test21() throws InterruptedException {
        CompletableFuture[] futures = findPricesStream("肥皂", "100001", shops)
                .map(f -> f.thenAccept(System.out::println))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        // 防止主线程结束导致应用退出
        TimeUnit.SECONDS.sleep(10);
    }

    public static Stream<CompletableFuture<Double>> findPricesStream(String product, String memberNo, List<Shop> shops) {
        return shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPriceQuote(product, memberNo)))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future.thenCompose(quote -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote))));
    }

}
