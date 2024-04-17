package org.javamaster.invocationlab.admin.util;

import org.bson.types.ObjectId;
import org.javamaster.invocationlab.admin.serializer.BigDecimalToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.LongToJsonSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.SneakyThrows;
import org.javamaster.invocationlab.admin.serializer.ObjectIdSerializer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.javamaster.invocationlab.admin.config.MvcConfig.configMapper;

/**
 * @author yudong
 */
@SuppressWarnings("VulnerableCodeUsages")
public class JsonUtils {
    public static final String STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static ObjectMapper mapper;

    public static ObjectMapper jacksonObjectMapper;

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat(STANDARD_PATTERN);
        mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setDateFormat(dateFormat)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigDecimal.class, BigDecimalToJsonSerializer.INSTANCE);
        simpleModule.addSerializer(Long.class, new LongToJsonSerializer());
        simpleModule.addSerializer(ObjectId.class, ObjectIdSerializer.INSTANCE);
        mapper.registerModule(simpleModule);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(STANDARD_PATTERN)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        mapper.registerModule(javaTimeModule);

        jacksonObjectMapper = new ObjectMapper();

        configMapper(jacksonObjectMapper);

        jacksonObjectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @SneakyThrows
    public static String objectToString(Object object) {
        return mapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T parseObject(String jsonString, Class<T> tClass) {
        return mapper.readValue(jsonString, tClass);
    }

    @SneakyThrows
    public static Object parseObject(String jsonString, JavaType javaType) {
        return mapper.readValue(jsonString, javaType);
    }

    public static boolean isJsonObjOrArray(String str) {
        String trim = str.trim();
        return isWrap(trim, '{', '}') || isWrap(trim, '[', ']');
    }

    private static boolean isWrap(CharSequence str, char prefixChar, char suffixChar) {
        if (null == str) {
            return false;
        } else {
            return str.charAt(0) == prefixChar && str.charAt(str.length() - 1) == suffixChar;
        }
    }
}
