package org.javamaster.invocationlab.admin.redis.impl

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo
import org.javamaster.invocationlab.admin.model.redis.ValueVo
import org.javamaster.invocationlab.admin.redis.AbstractRedisStrategy
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.util.SerializationUtils.convertValToObj
import org.javamaster.invocationlab.admin.util.SerializationUtils.dealJdkDeserialize
import org.javamaster.invocationlab.admin.util.SerializationUtils.isJdkSerialize
import org.javamaster.invocationlab.admin.util.SerializationUtils.serialize
import org.springframework.data.redis.connection.DataType
import org.springframework.data.redis.connection.RedisConnection
import java.nio.charset.StandardCharsets

class StringRedisStrategy : AbstractRedisStrategy() {
    override fun getValue(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo {
        val valueVo = createValueVo(connection, keyPair)

        val bytesValue = connection.stringCommands()[keyPair.left] ?: return valueVo
        valueVo.redisValueSize = bytesValue.size

        if (isJdkSerialize(bytesValue)) {
            val pair = dealJdkDeserialize(bytesValue)
            valueVo.redisValue = pair.left
            valueVo.redisValueClazz = pair.right
            valueVo.redisValueJdkSerialize = true
        } else {
            val value = String(bytesValue, StandardCharsets.UTF_8)
            valueVo.redisValue = value
            valueVo.redisValueJdkSerialize = false
        }

        return valueVo
    }

    override fun saveValue(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long {
        val oldValueVo = getValue(connection, keyPair)

        val b =
            saveValue(
                connection,
                keyPair,
                commonRedisVo.redisValue!!,
                commonRedisVo.redisValueJdkSerialize!!,
                oldValueVo
            )
        return if (b) 1L else 0L
    }

    private fun saveValue(
        connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, valNew: String,
        valNewJdkSerialize: Boolean, oldValueVo: ValueVo?
    ): Boolean {
        val keyBytes = keyPair.left
        val stringCommands = connection.stringCommands()

        val bytesNewVal: ByteArray
        if (oldValueVo == null) {
            if (valNewJdkSerialize) {
                val pair = resolveVal(valNew)
                val obj = convertValToObj(pair.left, pair.right)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = valNew.toByteArray(StandardCharsets.UTF_8)
            }
        } else {
            if (oldValueVo.redisValueJdkSerialize!!) {
                val obj = convertValToObj(valNew, oldValueVo.redisValueClazz!!)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = valNew.toByteArray(StandardCharsets.UTF_8)
            }
        }

        return stringCommands.set(keyBytes, bytesNewVal)!!
    }

    override fun addKey(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        saveValue(connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, null)

        return getValue(connection, keyPair)
    }

    override fun renameKey(
        connection: RedisConnection, oldKeyPair: Pair<ByteArray, Class<*>>,
        keyPair: Pair<ByteArray, Class<*>>, commonRedisVo: CommonRedisVo
    ): ValueVo {
        val oldValueVo = getValue(connection, oldKeyPair)

        saveValue(connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, oldValueVo)

        delKey(connection, oldKeyPair)

        return getValue(connection, keyPair)
    }

    override fun delField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long {
        throw UnsupportedOperationException()
    }

    override fun addField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): String {
        throw UnsupportedOperationException()
    }

    override fun dataType(): DataType {
        return DataType.STRING
    }
}
