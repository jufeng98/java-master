package org.javamaster.b2c.test.multithread;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void test() {
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
