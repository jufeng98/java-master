package org.javamaster.invocationlab.admin.redis

import org.javamaster.invocationlab.admin.model.redis.FieldVo
import org.javamaster.invocationlab.admin.model.redis.ValueVo
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.util.JsonUtils.isJsonObjOrArray
import org.javamaster.invocationlab.admin.util.SerializationUtils.dealJdkDeserialize
import org.javamaster.invocationlab.admin.util.SerializationUtils.isJdkSerialize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnection
import java.nio.charset.StandardCharsets
import java.util.*


abstract class AbstractRedisStrategy : RedisStrategy {
    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    fun createValueVo(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo {
        val keyBytes = keyPair.left
        val clazz = keyPair.right

        val ttl = connection.keyCommands().ttl(keyBytes)

        val valueVo: ValueVo = ValueVo().apply {
            redisKeyTtl = ttl
            redisKeyType = dataType().code()
            redisKeyType = dataType().code()
            redisKeyClazz = clazz
            fieldCount = 0
            fieldVos = emptyList()
        }
        if (isJdkSerialize(keyBytes)) {
            val pair = dealJdkDeserialize(keyBytes)
            valueVo.redisKey = pair.left
            valueVo.redisKeyJdkSerialize = true
            valueVo.redisKeyBase64 = Base64.getEncoder().encodeToString(keyBytes)
        } else {
            valueVo.redisKey = String(keyBytes, StandardCharsets.UTF_8)
            valueVo.redisKeyJdkSerialize = false
        }
        return valueVo
    }

    override fun setTtlIfNecessary(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        redisKeyTtl: Long
    ): Boolean {
        val keyBytes = keyPair.left
        val persist = if (-1L == redisKeyTtl) {
            connection.keyCommands().persist(keyBytes)
        } else {
            connection.keyCommands().expire(keyBytes, redisKeyTtl)
        }
        log.info("设置persist结果:{}", persist)
        return persist!!
    }

    override fun delKey(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): Long {
        val keyBytes = keyPair.left
        return connection.keyCommands().del(keyBytes)!!
    }

    protected fun findFieldVo(fieldVos: List<FieldVo>, fieldVal: String): Pair<FieldVo?, Int> {
        var index = -1
        var fieldVoOld: FieldVo? = null
        for (i in fieldVos.indices) {
            val fieldVo = fieldVos[i]
            if (fieldVo.fieldValue == fieldVal) {
                index = i
                fieldVoOld = fieldVo
                break
            }
        }
        return Pair.of(fieldVoOld, index)
    }

    fun resolveVal(s: String): Pair<String, Class<*>> {
        if (isJsonObjOrArray(s)) {
            return Pair.of(s, Void::class.java)
        }
        return RedisHelper.resolveVal(s)
    }
}
