package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.TimeUnit;

/**
 * 折扣服务
 *
 * @author yudong
 * @date 2019/1/8
 */
public class Discount {

    /**
     * 获取折扣价格
     */
    public static double applyDiscount(Quote quote) {
        try {
            // 模拟Discount服务响应的延迟
            TimeUnit.MILLISECONDS.sleep(500 + RandomUtils.nextInt(10, 1000));
        } catch (InterruptedException ignored) {
        }
        // 模拟折扣价格
        return quote.getPrice() * (100 - quote.getMemberLevelEnum().percentage) / 100;

    }

    /**
     * 会员等级
     */
    public enum MemberLevelEnum {
        NONE(0),
        SILVER(5),
        GOLD(10),
        PLATINUM(15),
        DIAMOND(20);

        public final int percentage;

        MemberLevelEnum(int percentage) {
            this.percentage = percentage;
        }

    }
}
