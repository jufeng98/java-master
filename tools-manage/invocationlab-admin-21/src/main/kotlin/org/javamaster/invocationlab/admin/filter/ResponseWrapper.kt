package org.javamaster.invocationlab.admin.filter

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.nio.charset.StandardCharsets


/**
 * @author yudong
 */
class ResponseWrapper(response: HttpServletResponse?) : HttpServletResponseWrapper(response) {
    private val outputStream = ServletByteArrayOutputStream()

    override fun getWriter(): PrintWriter {
        return PrintWriter(outputStream)
    }

    override fun getOutputStream(): ServletOutputStream {
        return outputStream
    }

    override fun toString(): String {
        return String(outputStream.toByteArray(), StandardCharsets.UTF_8)
    }
}

internal class ServletByteArrayOutputStream : ServletOutputStream() {
    private val buf = ByteArrayOutputStream()

    override fun isReady(): Boolean {
        return false
    }

    override fun setWriteListener(listener: WriteListener) {
    }

    override fun write(b: Int) {
        buf.write(b)
    }

    fun toByteArray(): ByteArray {
        return buf.toByteArray()
    }
}
