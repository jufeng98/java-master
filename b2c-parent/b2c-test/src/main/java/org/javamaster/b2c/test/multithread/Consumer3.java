package org.javamaster.b2c.test.multithread;

import java.util.concurrent.BlockingQueue;

/**
 * @author yudong
 * @date 2019/12/10
 */
public class Consumer3 {
    /**
     * 容量为1的缓冲池
     */
    private final BlockingQueue<String> cachePoolQueue;

    public Consumer3(BlockingQueue<String> cachePoolQueue) {
        this.cachePoolQueue = cachePoolQueue;
    }

    public void consume() throws InterruptedException {
        // 会一直阻塞直到能获取元素
        String orderCode = cachePoolQueue.take();
        System.out.println("线程名称" + Thread.currentThread().getName() + " 消费者消费订单号:" + orderCode);
    }
}
