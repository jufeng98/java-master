package org.javamaster.invocationlab.admin.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author yudong
 * @date 2022/11/12
 */
object ClassUtils {
    @JvmStatic
    fun getParamGenericType(type: Type?): Type? {
        if (type is ParameterizedType) {
            val actualTypeArguments = type.actualTypeArguments
            return actualTypeArguments[0]
        }
        return null
    }

    @JvmStatic
    fun shouldNew(type: Type?): Boolean {
        if (type == null) {
            return false
        }
        val typeName = type.typeName
        return shouldNew(typeName)
    }

    private fun shouldNew(typeName: String): Boolean {
        return !isPrimitive(typeName) && !typeName.startsWith("java")
    }

    @JvmStatic
    fun isPrimitive(typeName: String?): Boolean {
        return when (typeName) {
            "int", "char", "long", "boolean", "float", "double", "[B", "byte[]", "java.lang.String", "java.lang.Integer", "java.lang.Long", "java.lang.Double", "java.lang.Float", "java.math.BigInteger", "java.math.BigDecimal" -> true
            else -> false
        }
    }
}
