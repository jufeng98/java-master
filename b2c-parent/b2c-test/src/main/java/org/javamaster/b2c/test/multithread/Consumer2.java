package org.javamaster.b2c.test.multithread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yudong
 * @date 2019/12/10
 */
public class Consumer2 {
    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 容量为1的缓冲池
     */
    private final Queue<String> cachePoolQueue;

    private ReentrantLock reentrantLock;
    private Condition condition;

    public Consumer2(Queue<String> cachePoolQueue, ReentrantLock reentrantLock, Condition condition) {
        this.cachePoolQueue = cachePoolQueue;
        this.reentrantLock = reentrantLock;
        this.condition = condition;
    }

    public void consume() {
        reentrantLock.lock();
        try {
            while (cachePoolQueue.size() == 0) {
                condition.await();
            }
            String orderCode = cachePoolQueue.poll();
            System.out.println("线程名称" + Thread.currentThread().getName() + " 消费者消费订单号:" + orderCode);
            condition.signalAll();
        } catch (InterruptedException e) {
            logger.error("interrupted", e);
        } finally {
            reentrantLock.unlock();
        }
    }
}
