package org.javamaster.b2c.test.multithread;

import java.util.concurrent.RecursiveTask;

/**
 * @author yudong
 * @date 2019/12/9
 */
public class SumRecursiveTask extends RecursiveTask<Long> {

    private final long[] numbers;
    private final int start;
    private final int end;

    public static final long THRESHOLD = 10_000;

    public SumRecursiveTask(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    // 私有构造函数用于以递归方式为主任务创建子任务
    private SumRecursiveTask(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            return computeSequentially();
        }
        // 对任务分片,创建子任务
        SumRecursiveTask leftTask = new SumRecursiveTask(numbers, start, start + length / 2);
        leftTask.fork();
        SumRecursiveTask rightTask = new SumRecursiveTask(numbers, start + length / 2, end);
        Long rightResult = rightTask.compute();
        Long leftResult = leftTask.join();
        // 归并任务结果
        return leftResult + rightResult;
    }

    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }
}
