package org.javamaster.b2c.test.multithread;

import static org.javamaster.b2c.test.multithread.Shop.delay;

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
        delay();
        return Math.random() + 0.01;
    }
}
