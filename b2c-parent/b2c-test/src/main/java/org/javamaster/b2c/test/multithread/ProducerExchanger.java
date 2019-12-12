package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.concurrent.Exchanger;

/**
 * @author yudong
 * @date 2019/12/12
 */
public class ProducerExchanger {

    private Exchanger<List<String>> exchanger;
    private List<String> cacheList;

    public ProducerExchanger(Exchanger<List<String>> exchanger, List<String> cacheList) {
        this.exchanger = exchanger;
        this.cacheList = cacheList;
    }

    public void produce() {
        String orderCode = RandomStringUtils.randomAlphabetic(10);
        System.out.println("生产者线程" + Thread.currentThread().getName() + "放入订单号" + orderCode);
        cacheList.add(orderCode);
        try {
            cacheList = exchanger.exchange(cacheList);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
