package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/12/11
 */
public class Runner implements Callable<Integer> {

    private CountDownLatch judgeCountDownLatch;
    private CountDownLatch runnerCountDownLatch;

    public Runner(CountDownLatch judgeCountDownLatch, CountDownLatch runnerCountDownLatch) {
        this.judgeCountDownLatch = judgeCountDownLatch;
        this.runnerCountDownLatch = runnerCountDownLatch;
    }

    @Override
    public Integer call() throws Exception {
        // 等待裁判的发令枪响,即judgeCountDownLatch的count计数值没变为0之前代码阻塞在这一行
        judgeCountDownLatch.await();
        // 模拟跑步任务执行耗时
        int time = RandomUtils.nextInt(5, 10);
        TimeUnit.SECONDS.sleep(time);
        System.out.println("运动员线程" + Thread.currentThread().getName() + "跑到终点,耗时" + time + "秒");
        // 跑到终点,即将runnerCountDownLatch的count计数值减一
        runnerCountDownLatch.countDown();
        // 返回跑步任务耗时
        return time;
    }
}
