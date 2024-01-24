package org.javamaster.invocationlab.admin.util;


import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2023-12-30
 */
public class ExceptionUtils {

    public static String getSimplifyStackTrace(Throwable t) {
        StackTraceElement[] stackTrace = t.getStackTrace();
        String trace = Arrays.stream(stackTrace)
                .filter(it -> it.getLineNumber() != -1 && it.getClassName().startsWith("org.javamaster"))
                .limit(5)
                .map(it -> "\tat " + it)
                .collect(Collectors.joining("\r\n"));
        String error = t + "\r\n" + trace;
        if (t.getCause() != null) {
            String causeError = org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(t.getCause());
            error = error + "\r\nCaused by:" + causeError;
        }
        return error;
    }

}
