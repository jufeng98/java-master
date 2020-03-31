package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/7/2
 */
public class Shop {

    /**
     * 商店名称
     */
    private String name;

    Shop(String name) {
        this.name = name;
    }

    /**
     * 获取商品价格(同步)
     */
    public double getPrice(String product) {
        // 会查询商店的数据库，但也有可能执行一些其他耗时的任务，比如联系其
        // 他外部服务（比如，商店的供应商，或者跟制造商相关的推广折扣）
        // 模拟这些工作的耗时
        try {
            TimeUnit.MILLISECONDS.sleep(500 + RandomUtils.nextInt(10, 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return RandomUtils.nextDouble(1, 10) * product.charAt(0) + product.charAt(1);
    }

    /**
     * 获取商品价格(异步)
     */
    public Future<Double> getPriceAsync(String product) {
        // 创建CompletableFuture对象，它会包含计算的结果
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500 + RandomUtils.nextInt(10, 1000));
                double price = RandomUtils.nextDouble(1, 10) * product.charAt(0) + product.charAt(1);
                // 需长时间计算的任务结束并得出结果时，设置Future的返回值
                futurePrice.complete(price);
            } catch (Exception ex) {
                // 抛出导致失败的异常，完成这次Future操作
                futurePrice.completeExceptionally(ex);
            }
        }).start();
        // 无需等待还没结束的计算，直接返回Future对象
        return futurePrice;
    }


    public Future<Double> getPriceAsync1(String product) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500 + RandomUtils.nextInt(10, 1000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            double price = RandomUtils.nextDouble(1, 10) * product.charAt(0) + product.charAt(1);
            return price;
        });
    }

    /**
     * 获取会员等级和此等级对应的商品价格
     *
     * @param product
     * @param memberNo
     * @return
     */
    public String getPriceQuote(String product, String memberNo) {
        return calculatePriceQuote(product, memberNo);
    }

    private String calculatePriceQuote(String product, String memberNo) {
        delay();
        double price = calculatePrice(product);
        // 模拟会员的等级
        Discount.Code code = Discount.Code.values()[RandomUtils.nextInt(0, Discount.Code.values().length)];
        return String.format("%s:%.2f:%s", name, price, code);
    }

    /**
     * 模拟商品价格
     *
     * @param product
     * @return
     */
    private double calculatePrice(String product) {
        delay();
        return RandomUtils.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    /**
     * 模拟业务处理过程的耗时
     */
    public static void delay() {
        try {
            TimeUnit.MILLISECONDS.sleep(500 + RandomUtils.nextInt(10, 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }
}
