package org.javamaster.invocationlab.admin.redis.impl

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo
import org.javamaster.invocationlab.admin.model.redis.FieldVo
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
import java.util.stream.Collectors

class ZSetRedisStrategy : AbstractRedisStrategy() {
    override fun getValue(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo {
        val valueVo = createValueVo(connection, keyPair)

        val keyBytes = keyPair.left
        val len = connection.zSetCommands().zCard(keyBytes) ?: return valueVo
        valueVo.fieldCount = len.toInt()
        valueVo.redisValue = ""

        val tuples = connection.zSetCommands().zRangeWithScores(
            keyBytes, 0, 300
        )
        val fieldVos = tuples!!.stream()
            .map { tuple ->
                val fieldVo: FieldVo = FieldVo().apply {
                    fieldScore = tuple.score
                    fieldValueSize = tuple.value.size
                }

                if (isJdkSerialize(tuple.value)) {
                    val pair = dealJdkDeserialize(tuple.value)
                    fieldVo.fieldValue = pair.left
                    fieldVo.fieldValueClazz = pair.right
                    fieldVo.fieldValueJdkSerialize = true
                } else {
                    fieldVo.fieldValue = String(tuple.value, StandardCharsets.UTF_8)
                    fieldVo.fieldValueJdkSerialize = false
                }
                fieldVo
            }
            .collect(Collectors.toList())

        valueVo.fieldVos = fieldVos

        return valueVo
    }

    override fun saveValue(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long {
        val valueVo = getValue(connection, keyPair)

        val oldFieldVo = findFieldVo(valueVo.fieldVos!!, commonRedisVo.oldRedisValue!!).left

        val b = saveField(
            connection, keyPair, oldFieldVo!!.fieldScore!!, commonRedisVo.redisValue!!,
            commonRedisVo.redisValueJdkSerialize!!, oldFieldVo
        )
        return if (b) 1L else 0L
    }

    override fun addKey(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        saveField(
            connection, keyPair, commonRedisVo.score!!,
            commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, null
        )

        return getValue(connection, keyPair)
    }

    override fun renameKey(
        connection: RedisConnection, oldKeyPair: Pair<ByteArray, Class<*>>, keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        val oldValueVo = getValue(connection, oldKeyPair)

        for (fieldVo in oldValueVo.fieldVos!!) {
            saveField(
                connection, keyPair, fieldVo.fieldScore!!, fieldVo.fieldValue!!,
                fieldVo.fieldValueJdkSerialize!!, null
            )
        }

        delKey(connection, oldKeyPair)

        return getValue(connection, keyPair)
    }

    override fun delField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long {
        val valueVo = getValue(connection, keyPair)

        val oldFieldVo = findFieldVo(valueVo.fieldVos!!, commonRedisVo.redisValue!!).left

        return delField(connection, keyPair, oldFieldVo!!)
    }

    private fun delField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        oldFieldVo: FieldVo
    ): Long {
        val keyBytes = keyPair.left

        val bytesNewVal: ByteArray
        if (oldFieldVo.fieldValueJdkSerialize!!) {
            val obj = convertValToObj(oldFieldVo.fieldValue, oldFieldVo.fieldValueClazz!!)
            bytesNewVal = serialize(obj!!)
        } else {
            bytesNewVal = oldFieldVo.fieldValue!!.toByteArray(StandardCharsets.UTF_8)
        }
        return connection.zSetCommands().zRem(keyBytes, bytesNewVal)!!
    }

    private fun saveField(
        connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, score: Double, fieldValue: String,
        fieldValueJdkSerialize: Boolean, oldFieldVo: FieldVo?
    ): Boolean {
        val keyBytes = keyPair.left
        val redisZSetCommands = connection.zSetCommands()

        val bytesNewVal: ByteArray
        if (oldFieldVo == null) {
            if (fieldValueJdkSerialize) {
                val pair = resolveVal(
                    fieldValue
                )
                val obj = convertValToObj(pair.left, pair.right)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = fieldValue.toByteArray(StandardCharsets.UTF_8)
            }
        } else {
            delField(connection, keyPair, oldFieldVo)

            if (oldFieldVo.fieldValueJdkSerialize!!) {
                val obj = convertValToObj(fieldValue, oldFieldVo.fieldValueClazz!!)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = fieldValue.toByteArray(StandardCharsets.UTF_8)
            }
        }
        return redisZSetCommands.zAdd(keyBytes, score, bytesNewVal)!!
    }

    override fun addField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): String {
        return saveField(
            connection, keyPair, commonRedisVo.score!!, commonRedisVo.redisValue!!,
            commonRedisVo.redisValueJdkSerialize!!, null
        ).toString() + ""
    }

    override fun dataType(): DataType {
        return DataType.ZSET
    }
}
