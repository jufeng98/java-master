package org.javamaster.b2c.test.multithread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/12/9
 */
public class Task2 extends Thread {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run() {
        try {
            // 模拟耗时操作
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            logger.error("interrupted", e);
        }
        System.out.println(getClass().getSimpleName() + "执行完成");
    }
}
