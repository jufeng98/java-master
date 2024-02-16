package org.javamaster.invocationlab.admin.util

import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author yudong
 */
object ParamUtils {
    private val LOG: Logger = LoggerFactory.getLogger(ParamUtils::class.java)
    private val OBJECT_MAPPER: ObjectMapper = ObjectMapper()

    fun getParamString(a: Any?): String {
        if (a == null) {
            return "null"
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(a)
        } catch (e: Exception) {
            LOG.error("{} can't serialize to json", a.javaClass, e)
            return a.toString()
        }
    }

    fun getParamsString(a: Array<Any>?): String {
        if (a == null) {
            return "null"
        }
        val iMax = a.size - 1
        if (iMax == -1) {
            return "[]"
        }
        val b = StringBuilder()
        b.append('[')
        var i = 0
        while (true) {
            if (a[i] is ServletRequest
                || a[i] is ServletResponse
                || a[i] is HttpSession
                || a[i] is JSONObject
                || a[i] is JsonNode
            ) {
                b.append(a[i])
            } else {
                b.append(getParamString(a[i]))
            }
            if (i == iMax) {
                return b.append(']').toString()
            }
            b.append(", ")
            i++
        }
    }
}
