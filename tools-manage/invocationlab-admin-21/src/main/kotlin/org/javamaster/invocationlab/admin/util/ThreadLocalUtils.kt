package org.javamaster.invocationlab.admin.util

import org.springframework.core.NamedThreadLocal

/**
 * @author yudong
 * @date 2022/11/11
 */
object ThreadLocalUtils {
    private val THREAD_LOCAL: ThreadLocal<HashMap<String, Any>> = NamedThreadLocal("ThreadLocalUtil")
    const val OLD_GAV_VERSION: String = "oldGavVersion"

    @JvmStatic
    fun <T> get(key: String): T? {
        return getOrDefault(key)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrDefault(key: String): T? {
        val map = THREAD_LOCAL.get()
        val obj = map[key]
        return obj as T
    }

    @JvmStatic
    fun set(key: String, obj: Any) {
        var map = THREAD_LOCAL.get()
        if (map == null) {
            map = HashMap()
            THREAD_LOCAL.set(map)
        }
        map[key] = obj
    }

    @JvmStatic
    fun remove(key: String) {
        val map = THREAD_LOCAL.get() ?: return
        map.remove(key)
        if (map.isEmpty()) {
            THREAD_LOCAL.remove()
        }
    }
}
