package org.javamaster.invocationlab.admin.filter

import com.google.common.collect.Sets
import jakarta.servlet.*
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

/**
 * @author yudong
 * @date 2023/8/5
 */
@WebFilter(urlPatterns = ["/*"])
class CompressFilter : Filter {
    private lateinit var excludePaths: Set<String>

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse

        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false")
        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*")
        res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        res.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)

        val requestURI = req.requestURI
        if (excludePaths.contains(requestURI)) {
            filterChain.doFilter(req, res)
            return
        }
        if (!isGipSupported(req)) {
            filterChain.doFilter(req, res)
            return
        }
        res.setHeader("Content-Encoding", "gzip")
        val responseWrapper = ResponseWrapper(res)
        filterChain.doFilter(req, responseWrapper)
        (responseWrapper.outputStream as ServletByteArrayOutputStream).use { originalOutputStream ->
            val byteArrayOutputStream = ByteArrayOutputStream()
            GZIPOutputStream(byteArrayOutputStream).use {
                it.write(originalOutputStream.toByteArray())
                it.finish()
                val realOutPutStream = res.outputStream
                byteArrayOutputStream.writeTo(realOutPutStream)
                realOutPutStream.flush()
            }
        }
    }


    private fun isGipSupported(req: HttpServletRequest): Boolean {
        val encoding = req.getHeader("Accept-Encoding")
        return encoding != null && encoding.contains("gzip")
    }

    override fun init(filterConfig: FilterConfig) {
        excludePaths = Sets.newHashSet(
            "/ncnb/project/exportErd",
            "/ncnb/queryInfo/exportSql",
            "/actuator/health"
        )
    }
}
