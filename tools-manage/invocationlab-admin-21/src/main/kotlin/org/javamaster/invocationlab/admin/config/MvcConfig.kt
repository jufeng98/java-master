package org.javamaster.invocationlab.admin.config

import org.javamaster.invocationlab.admin.inteceptor.AppInterceptor
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.javamaster.invocationlab.admin.util.JsonUtils.configMapper
import org.apache.commons.lang3.time.DateUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpHeaders
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer

/**
 * @author yudong
 * @date 2022/11/12
 */
@Configuration
class MvcConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedHeaders("*")
            .allowedMethods("*")
            .exposedHeaders(HttpHeaders.CONTENT_DISPOSITION)
            .allowCredentials(false)
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(AppInterceptor())
    }

    override fun configureMessageConverters(converters: List<HttpMessageConverter<*>>) {
        converters.forEach(Consumer { httpMessageConverter: HttpMessageConverter<*> ->
            if (httpMessageConverter is MappingJackson2HttpMessageConverter) {
                val objectMapper = httpMessageConverter.objectMapper
                configMapper(objectMapper)
            }
        })
    }

    @Bean
    fun dateConverter(): Converter<String, Date> {
        class DateConverter : Converter<String, Date> {
            override fun convert(source: String): Date {
                return DateUtils.parseDate(source, JsonUtils.STANDARD_PATTERN)
            }
        }
        return DateConverter()
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.messageConverters[1] = StringHttpMessageConverter(StandardCharsets.UTF_8)
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(6000)
        factory.setReadTimeout(10000)
        restTemplate.requestFactory = factory
        return restTemplate
    }

}
