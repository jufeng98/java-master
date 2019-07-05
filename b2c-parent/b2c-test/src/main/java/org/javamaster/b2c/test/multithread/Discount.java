package org.javamaster.b2c.test.multithread;

import java.text.DecimalFormat;

/**
 * Created on 2019/1/8.<br/>
 *
 * @author yudong
 */
public class Discount {
    /**
     * 应用折扣
     *
     * @param quote
     * @return
     */
    public static String applyDiscount(Quote quote) {
        return quote.getShopName() + " price is " + apply(quote.getPrice(), quote.getDiscountCode());
    }

    /**
     * 获取折扣价格
     *
     * @param price 原始价格
     * @param code  折扣比例
     * @return 折扣价格
     */
    private static double apply(double price, Discount.Code code) {
        DecimalFormat dFormat = new DecimalFormat("#.00");
        String string = dFormat.format(price * (100 - code.getPercentage()) / 100);
        double temp = Double.parseDouble(string);
        return temp;

    }

    public enum Code {
        NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

        private final int percentage;

        Code(int percentage) {
            this.percentage = percentage;
        }

        public int getPercentage() {
            return percentage;
        }
    }
}
