package org.javamaster.invocationlab.admin.util

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object SessionUtils {
    @JvmStatic
    fun <T> saveToSession(key: String?, obj: T) {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
        val session = requestAttributes!!.request.session
        session.setAttribute(key, obj)
    }

    @JvmStatic
    fun <T> getFromSession(key: String?): T {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
        val session = requestAttributes!!.request.session
        @Suppress("UNCHECKED_CAST")
        return session.getAttribute(key) as T
    }
}
