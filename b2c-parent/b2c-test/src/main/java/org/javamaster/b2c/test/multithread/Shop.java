package org.javamaster.b2c.test.multithread;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/7/2
 */
public class Shop {

    private String name;
    static Random random = new Random();

    public Shop(String name) {
        this.name = name;
    }

    /**
     * 获取商品价格
     *
     * @param product
     * @return
     */
    public double getPrice(String product) {
        return calculatePrice(product);
    }


    public Future<Double> getPriceAsync(String product) {
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(() -> {
            try {
                double price = calculatePrice(product);
                futurePrice.complete(price);
            } catch (Exception ex) {
                futurePrice.completeExceptionally(ex);
            }
        }).start();
        return futurePrice;
    }


    public Future<Double> getPriceAsync1(String product) {
        return CompletableFuture.supplyAsync(() -> calculatePrice(product));
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
        Discount.Code code = Discount.Code.values()[random.nextInt(Discount.Code.values().length)];
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
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    /**
     * 模拟业务处理过程的耗时
     */
    public static void delay() {
        try {
            TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }
}
