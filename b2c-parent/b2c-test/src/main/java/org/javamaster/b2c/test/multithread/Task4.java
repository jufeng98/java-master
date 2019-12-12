package org.javamaster.b2c.test.multithread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * @author yudong
 * @date 2019/12/9
 */
public class Task4 implements Callable<String> {

    private static ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Override
    public String call() throws ParseException {
        // 某个线程首次调用get方法时,会先调用initialValue方法
        SimpleDateFormat simpleDateFormat = threadLocal.get();
        for (int i = 0; i < 10000; i++) {
            Date date = simpleDateFormat.parse("2019-12-12 12:12:12");
            if (date.getTime() != 1576123932000L) {
                System.err.println("解析日期错误" + date);
            }
        }
        return getClass().getSimpleName() + " success";
    }

}
