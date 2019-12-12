package org.javamaster.b2c.test.multithread;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author yudong
 * @date 2019/12/11
 */
public class Judge implements Callable<Integer> {

    private CountDownLatch judgeCountDownLatch;
    private CountDownLatch runnerCountDownLatch;

    private List<Future<Integer>> futures = new Vector<>();


    public Judge(CountDownLatch judgeCountDownLatch, CountDownLatch runnerCountDownLatch) {
        this.judgeCountDownLatch = judgeCountDownLatch;
        this.runnerCountDownLatch = runnerCountDownLatch;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("裁判员线程" + Thread.currentThread().getName() + "发令枪响");
        // 发令枪响,即将judgeCountDownLatch的count计数值减一
        judgeCountDownLatch.countDown();
        // 等待所有运动员跑到终点,即runnerCountDownLatch的count计数值没变为0之前代码阻塞在这一行
        runnerCountDownLatch.await();
        // 统计平均成绩并返回
        System.out.println("所有运动员都已跑到终点,裁判员线程" + Thread.currentThread().getName() + "开始统计跑步结果");
        return (int) futures.stream().mapToInt(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).average().getAsDouble();
    }

    public void collectScore(Future<Integer> future) {
        futures.add(future);
    }
}
