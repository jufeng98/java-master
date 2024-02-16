package org.javamaster.invocationlab.admin.config

import org.javamaster.invocationlab.admin.util.JsonUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * redis连接相关的配置
 *
 * @author yudong
 */
@Configuration
class RedisConfig {
    @Value("\${redis.host}")
    private lateinit var host: String

    @Value("\${redis.pwd}")
    private lateinit var pwd: String

    @Value("\${redis.port}")
    private var port = 0

    @Value("\${redis.default.db}")
    private var db = 0

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = host
        configuration.port = port
        configuration.database = db
        configuration.setPassword(pwd)
        val clientConfiguration = JedisClientConfiguration
            .builder()
            .readTimeout(Duration.of(6, ChronoUnit.SECONDS))
            .connectTimeout(Duration.of(6, ChronoUnit.SECONDS))
            .build()
        return JedisConnectionFactory(configuration, clientConfiguration)
    }

    @Bean
    @Primary
    fun redisTemplate(): RedisTemplate<Any, Any> {
        val redisTemplate = RedisTemplate<Any, Any>()
        redisTemplate.connectionFactory = jedisConnectionFactory()
        return redisTemplate
    }

    @Bean
    fun redisTemplateJackson(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = jedisConnectionFactory()
        val stringRedisSerializer = StringRedisSerializer()
        redisTemplate.keySerializer = stringRedisSerializer
        redisTemplate.hashKeySerializer = stringRedisSerializer


        val jackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(JsonUtils.jacksonObjectMapper)
        redisTemplate.valueSerializer = jackson2JsonRedisSerializer
        redisTemplate.hashValueSerializer = jackson2JsonRedisSerializer
        return redisTemplate
    }

    @Bean
    fun stringRedisTemplate(): StringRedisTemplate {
        return StringRedisTemplate(jedisConnectionFactory())
    }
}
