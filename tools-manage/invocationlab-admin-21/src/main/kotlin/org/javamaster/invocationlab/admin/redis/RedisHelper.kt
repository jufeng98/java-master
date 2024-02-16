package org.javamaster.invocationlab.admin.redis

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator
import org.javamaster.invocationlab.admin.util.RedisUtils.redisNodes
import org.javamaster.invocationlab.admin.util.SerializationUtils
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.ClassUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Triple
import org.springframework.data.redis.connection.RedisClusterNode
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.jedis.JedisClusterConnection
import org.springframework.data.redis.core.RedisConnectionUtils
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisCluster
import redis.clients.jedis.Protocol
import redis.clients.jedis.exceptions.JedisDataException
import redis.clients.jedis.params.ScanParams
import redis.clients.jedis.params.ScanParams.SCAN_POINTER_START_BINARY
import redis.clients.jedis.resps.ScanResult
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class RedisHelper {
    fun getSingleDbs(jedis: Jedis): Pair<Int, Map<Int, Long>> {
        val map: MutableMap<Int, Long> = Maps.newHashMap()
        var dbCount = 0
        while (true) {
            try {
                jedis.select(dbCount++)
                if (SpringUtils.proEnv) {
                    map[dbCount - 1] = -1L
                } else {
                    map[dbCount - 1] = jedis.dbSize()
                }
            } catch (e: JedisDataException) {
                dbCount--
                break
            }
        }
        jedis.select(Protocol.DEFAULT_DATABASE)
        return Pair(dbCount, map)
    }

    fun getClusterDbs(jedisCluster: JedisCluster, redisTemplate: RedisTemplate<Any, Any>): Pair<Int, Map<Int, Long>> {
        if (SpringUtils.proEnv) {
            return Pair.of(1, mapOf(0 to -1))
        }
        val clusterConnection = RedisConnectionUtils
            .getConnection(redisTemplate.connectionFactory!!) as JedisClusterConnection
        val clusterGetNodes = clusterConnection.clusterGetNodes()
        val total = clusterGetNodes
            .filter { it.type == RedisNode.NodeType.MASTER }
            .sumOf {
                clusterConnection.serverCommands().dbSize(it)
            }
        return Pair(1, mapOf(0 to total))
    }

    fun getSingleDbKeys(jedis: Jedis, count: Int, pattern: String): List<Triple<String, String, Class<*>>> {
        val list: MutableList<Triple<String, String, Class<*>>> = Lists.newArrayList()
        var cursor: ByteArray = SCAN_POINTER_START_BINARY
        val scanParams = ScanParams()
        scanParams.count(count)
        scanParams.match(pattern)
        do {
            val scanResult: ScanResult<ByteArray> = jedis.scan(cursor, scanParams)
            val resultBytesList: List<ByteArray> = scanResult.result
            for (resultBytes in resultBytesList) {
                if (SerializationUtils.isJdkSerialize(resultBytes)) {
                    val base64 = Base64.getEncoder().encodeToString(resultBytes)
                    val pair = SerializationUtils.dealJdkDeserialize(resultBytes)
                    list.add(Triple.of(pair.left, base64, pair.right))
                } else {
                    val key = String(resultBytes, StandardCharsets.UTF_8)
                    list.add(Triple.of(key, "", null))
                }
            }
            if (list.size >= 100) {
                break
            }
            cursor = scanResult.cursorAsBytes
        } while (!SCAN_POINTER_START_BINARY.contentEquals(cursor))
        return list
    }

    fun getClusterDbKeys(
        clusterConnection: JedisClusterConnection,
        connectionVo: ConnectionVo, count: Int, pattern: String
    ): List<Triple<String, String, Class<*>>> {
        val list: MutableList<Triple<String, String, Class<*>>> = Lists.newArrayList()
        val clusterNodes: Set<RedisNode> = redisNodes(connectionVo.nodes!!)
        val scanOptions: ScanOptions = ScanOptions.scanOptions().count(count.toLong()).match(pattern).build()
        val keys: MutableSet<String> = Sets.newHashSet()
        outer@ for (clusterNode in clusterNodes) {
            val redisClusterNode = RedisClusterNode(
                Objects.requireNonNull<String>(clusterNode.host),
                Objects.requireNonNull<Int>(clusterNode.port)
            )
            var reachLimit = false
            clusterConnection.scan(redisClusterNode, scanOptions).use { cursor ->
                while (cursor.hasNext()) {
                    val resultBytes: ByteArray = cursor.next()
                    var base64 = ""
                    var key: String
                    var clazz: Class<*> = Void::class.java
                    if (SerializationUtils.isJdkSerialize(resultBytes)) {
                        base64 = Base64.getEncoder().encodeToString(resultBytes)
                        val pair = SerializationUtils.dealJdkDeserialize(resultBytes)
                        key = pair.left
                        clazz = pair.right
                    } else {
                        key = String(resultBytes, StandardCharsets.UTF_8)
                    }
                    if (!keys.contains(key)) {
                        list.add(Triple.of(key, base64, clazz))
                        keys.add(key)
                    }
                    if (list.size >= 100) {
                        reachLimit = true
                        break
                    }
                }
            }
            if (reachLimit) {
                break@outer
            }
        }
        return list
    }

    fun convertKeyToBytes(redisKey: String, redisKeyBase64: String): Pair<ByteArray, Class<*>> {
        val keyBytes: ByteArray
        val keyClazz: Class<*>
        if (StringUtils.isNotBlank(redisKeyBase64)) {
            keyBytes = Base64.getDecoder().decode(redisKeyBase64)
            val pair = SerializationUtils.dealJdkDeserialize(keyBytes)
            keyClazz = pair.right
        } else {
            keyBytes = redisKey.toByteArray(StandardCharsets.UTF_8)
            keyClazz = Void::class.java
        }
        return Pair.of(keyBytes, keyClazz)
    }

    fun convertKeyToBytes(redisKey: String, redisKeyJdkSerialize: Boolean): Pair<ByteArray, Class<*>> {
        val keyBytes: ByteArray
        val keyClazz: Class<*>
        if (BooleanUtils.isTrue(redisKeyJdkSerialize)) {
            val pair = resolveVal(redisKey)
            val obj = SerializationUtils.convertValToObj(pair.left, pair.right)
            keyBytes = SerializationUtils.serialize(obj!!)
            keyClazz = pair.right
        } else {
            keyBytes = redisKey.toByteArray(StandardCharsets.UTF_8)
            keyClazz = Void::class.java
        }
        return Pair.of(keyBytes, keyClazz)
    }

    companion object {
        fun resolveVal(s: String): Pair<String, Class<*>> {
            try {
                val split = s.split("♣".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (split.size > 1) {
                    val clazz = ClassUtils.getClass(JdkCreator.globalJdkClassLoader, split[1])
                    return Pair.of(
                        split[0], clazz
                    )
                }
                return Pair.of(s, String::class.java)
            } catch (e: ClassNotFoundException) {
                throw BizException(s + "类型不存在!!!")
            }
        }
    }
}
