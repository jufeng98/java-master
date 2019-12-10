package org.javamaster.b2c.test.multithread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/12/9
 */
public class Task3 implements Callable<String> {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String call() {
        try {
            // 模拟耗时操作
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            logger.error("interrupted", e);
        }
        System.out.println(getClass().getSimpleName() + "执行完成");
        return getClass().getSimpleName() + " success";
    }
}
