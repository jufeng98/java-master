package org.javamaster.invocationlab.admin.util

import jakarta.servlet.http.Cookie
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * @author yudong
 */
object CookieUtils {
    @JvmStatic
    fun getCookieValue(name: String): String {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
        val request = requestAttributes!!.request
        val cookies = request.cookies ?: return ""
        val list = Arrays.stream(cookies)
            .filter { cookie: Cookie -> name == cookie.name }
            .toList()
        if (list.isEmpty()) {
            return ""
        }
        return list.first().value
    }
}
