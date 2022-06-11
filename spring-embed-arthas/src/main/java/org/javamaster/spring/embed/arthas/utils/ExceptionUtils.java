package org.javamaster.spring.embed.arthas.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author yudong
 * @date 2022/6/4
 */
public class ExceptionUtils {

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

}
