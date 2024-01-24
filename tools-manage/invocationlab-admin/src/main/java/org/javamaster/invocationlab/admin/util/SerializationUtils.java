package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.core.convert.ConversionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SerializationUtils {
    private static final byte AC = HexUtils.fromHexString("AC")[0];
    private static final byte ED = HexUtils.fromHexString("ED")[0];

    @SneakyThrows
    public static byte[] serialize(Object object) {
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException(object.getClass().getSimpleName() + " requires a Serializable payload " +
                    "but received an object of type [" + object.getClass().getName() + "]");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        @Cleanup
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        return outputStream.toByteArray();
    }

    @SneakyThrows
    public static Object deserialize(byte[] bytes, ClassLoader classLoader) {
        @Cleanup
        InputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ConfigurableObjectInputStream(inputStream, classLoader);
        return objectInputStream.readObject();
    }

    public static Pair<String, Class<?>> dealJdkDeserialize(byte[] bytes) {
        String val;
        Class<?> clazz = Void.class;
        try {
            Object deserializeObj = SerializationUtils.deserialize(bytes, JdkCreator.globalJdkClassLoader);
            clazz = deserializeObj.getClass();
            if (ClassUtils.isPrimitive(clazz.getTypeName())) {
                val = deserializeObj.toString();
            } else {
                val = JsonUtils.jacksonObjectMapper.writeValueAsString(deserializeObj);
            }
        } catch (Exception e) {
            String value = new String(bytes, StandardCharsets.UTF_8);
            log.error(value, e);
            val = "无法反序列化" + e.getMessage() + ",缺少class,请先在解析Redis JDK序列化依赖菜单解析依赖!!!\r\n\r\n" + value;
        }
        return new Pair<>(val, clazz);
    }

    @SneakyThrows
    public static Object convertValToObj(String val, Class<?> clazz) {
        if (clazz == Void.class) {
            clazz = JsonUtils.isJsonObjOrArray(val) ? Object.class : String.class;
        }

        if (ClassUtils.isPrimitive(clazz.getTypeName())) {
            ConversionService conversionService = SpringUtils.getContext().getBean(ConversionService.class);
            return conversionService.convert(val, clazz);
        } else {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(JdkCreator.globalJdkClassLoader);
                Object obj = JsonUtils.jacksonObjectMapper.readValue(val, Object.class);
                if (obj == null) {
                    throw new BizException("无法反序列化!!!");
                }
                return obj;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        }
    }

    public static boolean isJdkSerialize(byte[] bytes) {
        return bytes.length > 1 && bytes[0] == AC && bytes[1] == ED;
    }
}
