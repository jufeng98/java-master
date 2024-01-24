package org.javamaster.invocationlab.admin.util;

import org.springframework.core.NamedThreadLocal;

import java.util.HashMap;

/**
 * @author yudong
 * @date 2022/11/11
 */
public class ThreadLocalUtils {
    private static final ThreadLocal<HashMap<String, Object>> THREAD_LOCAL = new NamedThreadLocal<>("ThreadLocalUtil");
    public static final String OLD_GAV_VERSION = "oldGavVersion";

    public static <T> T get(String key) {
        return getOrDefault(key, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(String key, Object defaultVal) {
        HashMap<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            return (T) defaultVal;
        }
        Object obj = map.get(key);
        if (obj == null) {
            return (T) defaultVal;
        }
        return (T) obj;
    }

    public static void set(String key, Object obj) {
        HashMap<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        map.put(key, obj);
    }

    public static void remove(String key) {
        HashMap<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            return;
        }
        map.remove(key);
        if (map.isEmpty()) {
            THREAD_LOCAL.remove();
        }
    }

}
