package org.javamaster.invocationlab.admin.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yudong
 * @date 2022/11/12
 */
public class ExecutorUtils {

    public static ExecutorService executorService;

    static {
        /*
         * corePoolSize 线程池常驻核心线程数
         * maximumPoolSize 线程池任务最多时，最大可以创建的线程数
         * keepAliveTime 线程的存活时间，让线程池空闲，且超过了该时间，多余的线程会销毁
         * unit 线程存活时间单位
         * workQueue 线程池执行的任务队列，线程池所有线程都在执行任务时，来了任务会放到队列
         * threadFactory 线程池创建工厂
         * handler 拒绝策略，workQueue满了，也不能创建新的线程，会用到此策略
         * 		AbortPolicy:终止策略，抛异常
         * 		CallerRunsPolicy:任务交给当前线程来执行
         * 		DiscardPolicy:忽略此任务，最新的任务
         * 		DiscardOldestPolicy:忽略最早的任务（最新加入队列的任务）
         * 使用无界任务队列，线程池的任务队列可以无限制的添加新的任务，而线程池创建的最大线程数量就是corePoolSize设置
         * 的数量，也就是说在这种情况下maximumPoolSize这个参数是无效的，哪怕你的任务队列中缓存了很多未执行的任务，当线程
         * 池的线程数达到corePoolSize后，就不会再增加了；若后续有新的任务加入，则直接进入队列等待
         */
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 64,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory());
    }

    /**
     * 启动异步任务
     */
    public static void startAsyncTask(Runnable runnable) {
        executorService.submit(runnable);
    }

    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(Thread.currentThread().getThreadGroup(), r,
                    "pool-rpc-postman-" + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
