package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yudong
 * @date 2019/12/10
 */
public class Producer2 {
    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 容量为1的缓冲池
     */
    private final Queue<String> cachePoolQueue;
    private ReentrantLock reentrantLock;
    private Condition condition;

    public Producer2(Queue<String> cachePoolQueue, ReentrantLock reentrantLock, Condition condition) {
        this.cachePoolQueue = cachePoolQueue;
        this.reentrantLock = reentrantLock;
        this.condition = condition;
    }

    public void produce() {
        reentrantLock.lock();
        try {
            while (cachePoolQueue.size() == 1) {
                condition.await();
            }
            String orderCode = RandomStringUtils.randomAlphabetic(10);
            cachePoolQueue.add(orderCode);
            System.out.println("线程名称" + Thread.currentThread().getName() + " 生产者放入订单号:" + orderCode);
            condition.signalAll();
        } catch (InterruptedException e) {
            logger.error("interrupted", e);
        } finally {
            reentrantLock.unlock();
        }
    }
}
