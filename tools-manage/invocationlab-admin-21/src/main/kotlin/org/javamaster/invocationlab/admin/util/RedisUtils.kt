package org.javamaster.invocationlab.admin.util

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo
import org.javamaster.invocationlab.admin.service.impl.RedisServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

/**
 * @author yudong
 */
object RedisUtils {
    @JvmStatic
    fun getConnectionVo(connectId: String): ConnectionVo {
        val stringRedisTemplate = SpringUtils.context.getBean(
            StringRedisTemplate::class.java
        )
        val objectMapper = SpringUtils.context.getBean(
            ObjectMapper::class.java
        )
        val jsonStr = stringRedisTemplate.opsForHash<Any, Any>()[RedisServiceImpl.HASH_KEY_DBS, connectId] as String?
        if (StringUtils.isBlank(jsonStr)) {
            throw ErdException("连接" + connectId + "不存在")
        }
        try {
            return objectMapper.readValue(jsonStr, ConnectionVo::class.java)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun getRedisTemplate(connectId: String, db: Int): RedisTemplate<Any, Any> {
        val connectionVo = getConnectionVo(connectId)
        val beanFactory = SpringUtils.context
            .autowireCapableBeanFactory as DefaultListableBeanFactory
        val id = "$connectId:$db"
        if (beanFactory.containsBean(id)) {
            @Suppress("UNCHECKED_CAST")
            return beanFactory.getBean(id) as RedisTemplate<Any, Any>
        }
        val connectionFactory = getJedisConnectionFactory(connectionVo, db)
        val redisTemplate = RedisTemplate<Any, Any>()
        redisTemplate.connectionFactory = connectionFactory
        redisTemplate.afterPropertiesSet()
        beanFactory.registerSingleton(id, redisTemplate)
        return redisTemplate
    }

    @JvmStatic
    fun getJedisConnectionFactory(connectionVo: ConnectionVo, db: Int): JedisConnectionFactory {
        val connectionFactory = if (StringUtils.isNotBlank(connectionVo.nodes)) {
            getJedisConnectionFactoryCluster(connectionVo.nodes!!, connectionVo.password!!)
        } else {
            getJedisConnectionFactorySingle(
                connectionVo.host!!, connectionVo.port!!, db, connectionVo.password!!
            )
        }
        connectionFactory.connection
        return connectionFactory
    }

    private fun getJedisConnectionFactorySingle(host: String, port: Int, db: Int, pwd: String): JedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = host
        configuration.port = port
        configuration.database = db
        configuration.setPassword(pwd)
        val jedisConnectionFactory = JedisConnectionFactory(configuration)
        jedisConnectionFactory.afterPropertiesSet()
        return jedisConnectionFactory
    }

    @JvmStatic
    fun redisNodes(nodes: String): Set<RedisNode> {
        return Arrays.stream(nodes.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .map { s: String ->
                val split = s.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                RedisNode(split[0], split[1].toInt())
            }
            .collect(Collectors.toSet())
    }

    private fun getJedisConnectionFactoryCluster(nodes: String, pwd: String): JedisConnectionFactory {
        val nodesSet = redisNodes(nodes)
        val clusterConfiguration = RedisClusterConfiguration()
        clusterConfiguration.setClusterNodes(nodesSet)
        clusterConfiguration.setPassword(pwd)
        val jedisConnectionFactory = JedisConnectionFactory(clusterConfiguration)
        jedisConnectionFactory.afterPropertiesSet()
        return jedisConnectionFactory
    }
}
