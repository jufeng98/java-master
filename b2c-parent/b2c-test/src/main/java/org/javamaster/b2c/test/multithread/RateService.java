package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/7/2
 */
public class RateService {

    /**
     * 获取兑换汇率
     *
     * @param country1
     * @param country2
     * @return
     */
    public static double getRate(String country1, String country2) {
        try {
            // 模拟查询兑换汇率耗时
            TimeUnit.MILLISECONDS.sleep(500 + RandomUtils.nextInt(10, 1000));
        } catch (InterruptedException ignored) {
        }
        // 模拟兑换汇率
        double rate = (country1.length() + country2.length()) / 100.0;
        return Math.random() + rate;
    }
}
