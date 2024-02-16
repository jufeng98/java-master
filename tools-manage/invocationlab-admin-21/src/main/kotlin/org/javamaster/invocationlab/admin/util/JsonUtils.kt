package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.serializer.BigDecimalToJsonSerializer
import org.javamaster.invocationlab.admin.serializer.BigIntegerToJsonSerializer
import org.javamaster.invocationlab.admin.serializer.ByteArrayToJsonSerializer
import org.javamaster.invocationlab.admin.serializer.LongToJsonSerializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import java.math.BigDecimal
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author yudong
 */
object JsonUtils {
    const val STANDARD_PATTERN: String = "yyyy-MM-dd HH:mm:ss"

    @JvmField
    var mapper: ObjectMapper

    @JvmField
    var jacksonObjectMapper: ObjectMapper

    init {
        val dateFormat = SimpleDateFormat(STANDARD_PATTERN)
        mapper = ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .setDateFormat(dateFormat)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        @Suppress("DEPRECATION")
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

        val simpleModule = SimpleModule()
        simpleModule.addSerializer(BigDecimal::class.java, BigDecimalToJsonSerializer.INSTANCE)
        simpleModule.addSerializer(Long::class.java, LongToJsonSerializer())
        mapper.registerModule(simpleModule)

        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(
            LocalDate::class.java,
            LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        javaTimeModule.addSerializer(
            LocalDateTime::class.java, LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(
                    STANDARD_PATTERN
                )
            )
        )
        javaTimeModule.addSerializer(
            LocalTime::class.java,
            LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
        mapper.registerModule(javaTimeModule)

        jacksonObjectMapper = ObjectMapper()

        configMapper(jacksonObjectMapper)

        jacksonObjectMapper.activateDefaultTyping(
            jacksonObjectMapper.polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )
    }

    @JvmStatic
    fun configMapper(objectMapper: ObjectMapper) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

        val simpleModule = SimpleModule()
        simpleModule.addSerializer(BigDecimal::class.java, BigDecimalToJsonSerializer.INSTANCE)
        simpleModule.addSerializer(BigDecimal::class.javaObjectType, BigDecimalToJsonSerializer.INSTANCE)

        simpleModule.addSerializer(BigInteger::class.java, BigIntegerToJsonSerializer.INSTANCE)
        simpleModule.addSerializer(BigInteger::class.javaObjectType, BigIntegerToJsonSerializer.INSTANCE)

        simpleModule.addSerializer(Long::class.java, LongToJsonSerializer())
        simpleModule.addSerializer(Long::class.javaObjectType, LongToJsonSerializer())

        simpleModule.addSerializer(ByteArray::class.java, ByteArrayToJsonSerializer())
        simpleModule.addSerializer(ByteArray::class.javaObjectType, ByteArrayToJsonSerializer())
        objectMapper.registerModule(simpleModule)

        objectMapper.setTimeZone(TimeZone.getDefault())
        objectMapper.setDateFormat(SimpleDateFormat(STANDARD_PATTERN))

        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(
            LocalDate::class.java,
            LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        javaTimeModule.addSerializer(
            LocalDate::class.javaObjectType,
            LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )

        javaTimeModule.addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(DateTimeFormatter.ofPattern(STANDARD_PATTERN))
        )
        javaTimeModule.addSerializer(
            LocalDateTime::class.javaObjectType,
            LocalDateTimeSerializer(DateTimeFormatter.ofPattern(STANDARD_PATTERN))
        )

        javaTimeModule.addSerializer(
            LocalTime::class.java,
            LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
        javaTimeModule.addSerializer(
            LocalTime::class.javaObjectType,
            LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
        objectMapper.registerModule(javaTimeModule)
    }

    @JvmStatic
    fun objectToString(`object`: Any?): String {
        return mapper.writeValueAsString(`object`)
    }

    @JvmStatic
    fun <T> parseObject(jsonString: String?, tClass: Class<T>?): T {
        return mapper.readValue(jsonString, tClass)
    }


    fun parseObject(jsonString: String?, javaType: JavaType?): Any {
        return mapper.readValue(jsonString, javaType)
    }

    @JvmStatic
    fun isJsonObjOrArray(str: String): Boolean {
        val trim = str.trim { it <= ' ' }
        return isWrap(trim, '{', '}') || isWrap(trim, '[', ']')
    }

    private fun isWrap(str: CharSequence?, prefixChar: Char, suffixChar: Char): Boolean {
        return if (null == str) {
            false
        } else {
            str[0] == prefixChar && str[str.length - 1] == suffixChar
        }
    }
}
