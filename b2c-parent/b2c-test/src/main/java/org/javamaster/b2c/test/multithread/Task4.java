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

    private static ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<>();

    @Override
    public String call() throws ParseException {
        SimpleDateFormat simpleDateFormat = threadLocal.get();
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            threadLocal.set(simpleDateFormat);
        }
        for (int i = 0; i < 10000; i++) {
            Date date = simpleDateFormat.parse("2019-12-12 12:12:12");
            if (date.getTime() != 1576123932000L) {
                System.err.println("解析日期错误" + date);
            }
        }
        return getClass().getSimpleName() + " success";
    }

}
