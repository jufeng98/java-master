package org.javamaster.java.agent.collector;

import org.javamaster.java.agent.advice.Advice;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yudong
 * @date 2020/10/16
 */
public class WatchCollector {

    private static final ConcurrentHashMap<String, WatchListener> LISTENERS = new ConcurrentHashMap<>();

    public static void add(String key, Advice advice) throws Exception {
        for (WatchListener value : LISTENERS.values()) {
            value.call(key, advice);
        }
    }

    public static void addListener(String key, WatchListener watchListener) {
        LISTENERS.put(key, watchListener);
    }

    public static void removeListener(String key) {
        LISTENERS.remove(key);
    }

    public interface WatchListener {
        void call(String key, Advice advice) throws Exception;
    }

}
