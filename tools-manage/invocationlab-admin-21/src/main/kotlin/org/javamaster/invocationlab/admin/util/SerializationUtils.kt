package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator
import org.javamaster.invocationlab.admin.util.ClassUtils.isPrimitive
import org.javamaster.invocationlab.admin.util.JsonUtils.isJsonObjOrArray
import org.apache.tomcat.util.buf.HexUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.ConfigurableObjectInputStream
import org.springframework.core.convert.ConversionService
import java.io.*
import java.nio.charset.StandardCharsets


object SerializationUtils {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val AC = HexUtils.fromHexString("AC")[0]
    private val ED = HexUtils.fromHexString("ED")[0]

    @JvmStatic

    fun serialize(`object`: Any): ByteArray {
        require(`object` is Serializable) {
            `object`.javaClass.simpleName + " requires a Serializable payload " +
                    "but received an object of type [" + `object`.javaClass.name + "]"
        }
        val outputStream = ByteArrayOutputStream()
        ObjectOutputStream(outputStream).use {
            it.writeObject(`object`)
            it.flush()
            return outputStream.toByteArray()
        }
    }

    fun deserialize(bytes: ByteArray?, classLoader: ClassLoader?): Any {
        ByteArrayInputStream(bytes).use {
            val objectInputStream: ObjectInputStream = ConfigurableObjectInputStream(it, classLoader)
            return objectInputStream.readObject()
        }
    }

    @JvmStatic
    fun dealJdkDeserialize(bytes: ByteArray): Pair<String, Class<*>> {
        var `val`: String
        var clazz: Class<*> = Void::class.java
        try {
            val deserializeObj = deserialize(bytes, JdkCreator.globalJdkClassLoader)
            clazz = deserializeObj.javaClass
            `val` = if (isPrimitive(clazz.typeName)) {
                deserializeObj.toString()
            } else {
                JsonUtils.jacksonObjectMapper.writeValueAsString(deserializeObj)
            }
        } catch (e: Exception) {
            val value = String(bytes, StandardCharsets.UTF_8)
            log.error(value, e)
            `val` = """
                无法反序列化${e.message},缺少class,请先在解析Redis JDK序列化依赖菜单解析依赖!!!
                
                $value
                """.trimIndent()
        }
        return Pair(`val`, clazz)
    }

    @JvmStatic

    fun convertValToObj(`val`: String?, clazz: Class<*>): Any? {
        var cls = clazz
        if (cls == Void::class.java) {
            cls = if (isJsonObjOrArray(`val`!!)) Any::class.java else String::class.java
        }

        if (isPrimitive(cls.typeName)) {
            val conversionService = SpringUtils.context.getBean(
                ConversionService::class.java
            )
            return conversionService.convert(`val`, cls)
        } else {
            val contextClassLoader = Thread.currentThread().contextClassLoader
            try {
                Thread.currentThread().contextClassLoader = JdkCreator.globalJdkClassLoader
                val obj = JsonUtils.jacksonObjectMapper.readValue(`val`, Any::class.java)
                    ?: throw BizException("无法反序列化!!!")
                return obj
            } catch (e: Exception) {
                throw RuntimeException(e)
            } finally {
                Thread.currentThread().contextClassLoader = contextClassLoader
            }
        }
    }

    @JvmStatic
    fun isJdkSerialize(bytes: ByteArray): Boolean {
        return bytes.size > 1 && bytes[0] == AC && bytes[1] == ED
    }
}
