package org.javamaster.b2c.test.multithread;

import java.util.List;
import java.util.concurrent.Exchanger;

/**
 * @author yudong
 * @date 2019/12/12
 */
public class ConsumerExchanger {

    private Exchanger<List<String>> exchanger;
    private List<String> cacheList;

    public ConsumerExchanger(Exchanger<List<String>> exchanger, List<String> cacheList) {
        this.exchanger = exchanger;
        this.cacheList = cacheList;
    }

    public void consumer() {
        try {
            cacheList = exchanger.exchange(cacheList);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (String orderCode : cacheList) {
            System.out.println("消费者线程" + Thread.currentThread().getName() + "消费订单号" + orderCode);
        }
        cacheList.clear();
    }
}
