package org.javamaster.invocationlab.admin.redis

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo
import org.javamaster.invocationlab.admin.model.redis.ValueVo
import org.javamaster.invocationlab.admin.redis.impl.*
import org.javamaster.invocationlab.admin.service.Pair
import org.springframework.data.redis.connection.DataType
import org.springframework.data.redis.connection.RedisConnection

interface RedisStrategy {
    fun getValue(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo

    fun saveValue(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long

    fun addKey(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo

    fun delKey(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): Long

    fun renameKey(
        connection: RedisConnection, oldKeyPair: Pair<ByteArray, Class<*>>, keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo

    fun delField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long

    fun addField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): String

    fun setTtlIfNecessary(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        redisKeyTtl: Long
    ): Boolean

    fun dataType(): DataType

    companion object {
        @JvmStatic
        fun getInstance(dataType: DataType): RedisStrategy {
            return MAP[dataType]!!
        }

        val MAP: HashMap<DataType, RedisStrategy> = object : HashMap<DataType, RedisStrategy>() {
            init {
                put(DataType.STRING, StringRedisStrategy())
                put(DataType.HASH, HashRedisStrategy())
                put(DataType.LIST, ListRedisStrategy())
                put(DataType.SET, SetRedisStrategy())
                put(DataType.ZSET, ZSetRedisStrategy())
            }
        }
    }
}
