package org.javamaster.invocationlab.admin.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author yudong
 * @date 2022/11/12
 */
public class ClassUtils {

    public static Type getParamGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return actualTypeArguments[0];
        }
        return null;
    }

    public static boolean shouldNew(Type type) {
        if (type == null) {
            return false;
        }
        String typeName = type.getTypeName();
        return shouldNew(typeName);
    }

    public static boolean shouldNew(String typeName) {
        return !isPrimitive(typeName) && !typeName.startsWith("java");
    }

    public static boolean isPrimitive(String typeName) {
        switch (typeName) {
            case "int":
            case "char":
            case "long":
            case "boolean":
            case "float":
            case "double":
            case "[B":
            case "byte[]":
            case "java.lang.String":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Double":
            case "java.lang.Float":
            case "java.math.BigInteger":
            case "java.math.BigDecimal":
                return true;
            default:
                return false;
        }
    }
}
