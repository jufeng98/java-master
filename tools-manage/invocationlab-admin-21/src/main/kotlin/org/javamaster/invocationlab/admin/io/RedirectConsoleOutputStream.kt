package org.javamaster.invocationlab.admin.io

import java.io.ByteArrayOutputStream
import java.io.PrintStream


/**
 * @author yudong
 */
class RedirectConsoleOutputStream(private val byteArrayOutputStream: ByteArrayOutputStream) : PrintStream(
    byteArrayOutputStream
) {
    private val printStream: PrintStream = System.out

    override fun println(x: Any?) {
        printStream.println(x)
        super.println(x)
    }

    val logByteArray: ByteArray
        get() = byteArrayOutputStream.toByteArray()
}
