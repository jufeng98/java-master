package org.javamaster.b2c.classloader.utils;

import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;

/**
 * @author yudong
 * @date 2019/6/24
 */
public class SerializeUtils {
    public static Object deserialize(@Nullable byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            ConfigurableObjectInputStream inputStream =
                    new ConfigurableObjectInputStream(stream, Thread.currentThread().getContextClassLoader());
            return inputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("deserialize failed", e);
        }
    }
}
