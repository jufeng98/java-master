package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.BlockingQueue;

/**
 * @author yudong
 * @date 2019/12/10
 */
public class Producer3 {
    /**
     * 容量为1的缓冲池
     */
    private final BlockingQueue<String> cachePoolQueue;

    public Producer3(BlockingQueue<String> cachePoolQueue) {
        this.cachePoolQueue = cachePoolQueue;
    }

    public void produce() throws InterruptedException {
        String orderCode = RandomStringUtils.randomAlphabetic(10);
        // 会一直阻塞直到有空间能放入元素
        cachePoolQueue.put(orderCode);
        System.out.println("线程名称" + Thread.currentThread().getName() + " 生产者放入订单号:" + orderCode);
    }
}
