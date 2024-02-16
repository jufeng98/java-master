package org.javamaster.invocationlab.admin.util

import org.apache.commons.lang3.exception.ExceptionUtils
import java.util.*
import java.util.stream.Collectors


/**
 * @author yudong
 * @date 2023-12-30
 */
object ExceptionUtils {
    @JvmStatic
    fun getSimplifyStackTrace(t: Throwable): String {
        val stackTrace = t.stackTrace
        val trace = Arrays.stream(stackTrace)
            .filter { it: StackTraceElement -> it.lineNumber != -1 && it.className.startsWith("org.javamaster") }
            .limit(5)
            .map { it: StackTraceElement -> "\tat $it" }
            .collect(Collectors.joining("\r\n"))
        var error = """
             $t
             $trace
             """.trimIndent()
        if (t.cause != null) {
            val causeError = ExceptionUtils.getStackTrace(t.cause)
            error = "$error\r\nCaused by:$causeError"
        }
        return error
    }
}
