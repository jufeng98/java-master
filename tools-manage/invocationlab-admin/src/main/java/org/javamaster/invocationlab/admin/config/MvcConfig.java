package org.javamaster.invocationlab.admin.config;

import org.javamaster.invocationlab.admin.inteceptor.AppInterceptor;
import org.javamaster.invocationlab.admin.serializer.BigDecimalToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.BigIntegerToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.ByteArrayToJsonSerializer;
import org.javamaster.invocationlab.admin.serializer.LongToJsonSerializer;
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static org.javamaster.invocationlab.admin.util.JsonUtils.STANDARD_PATTERN;

/**
 * @author yudong
 * @date 2022/11/12
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private ErdOnlineUserService erdOnlineUserService;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**/*")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AppInterceptor(erdOnlineUserService));
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.forEach(httpMessageConverter -> {
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) httpMessageConverter).getObjectMapper();
                configMapper(objectMapper);
            }
        });
    }

    public static void configMapper(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigDecimal.class, BigDecimalToJsonSerializer.INSTANCE);
        simpleModule.addSerializer(BigInteger.class, BigIntegerToJsonSerializer.INSTANCE);
        simpleModule.addSerializer(Long.class, new LongToJsonSerializer());
        simpleModule.addSerializer(byte[].class, new ByteArrayToJsonSerializer());
        objectMapper.registerModule(simpleModule);

        objectMapper.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_PATTERN));

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(STANDARD_PATTERN)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(6000);
        factory.setReadTimeout(10000);
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}
