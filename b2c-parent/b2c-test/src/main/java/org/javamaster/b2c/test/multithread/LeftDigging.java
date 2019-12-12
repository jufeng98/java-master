package org.javamaster.b2c.test.multithread;

import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 * @date 2019/12/12
 */
public class LeftDigging implements Runnable {
    private CyclicBarrier cyclicBarrier;

    public LeftDigging(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            System.out.println("左边队伍任务挖掘中...");
            int time = RandomUtils.nextInt(3, 8);
            TimeUnit.SECONDS.sleep(time);
            System.out.println("左边队伍到达汇合点(即栅栏处),耗时" + time + "秒,等待中...");
            // 线程完成了其任务,停留在栅栏处,即线程阻塞在这一行.一旦所有线程都到达这个栅栏,那么栅栏就会被撤销,线程就可以继续运行了
            cyclicBarrier.await();
            System.out.println("栅栏被撤销,左边队伍线程继续运行");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
