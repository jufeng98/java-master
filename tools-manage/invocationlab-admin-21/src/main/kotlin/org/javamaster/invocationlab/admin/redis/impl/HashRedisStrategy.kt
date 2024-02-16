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
import java.util.stream.IntStream

class HashRedisStrategy : AbstractRedisStrategy() {
    override fun getValue(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo {
        val valueVo = createValueVo(connection, keyPair)

        val keyBytes = keyPair.left
        val fieldCount = connection.hashCommands().hLen(keyBytes) ?: return valueVo
        valueVo.fieldCount = fieldCount.toInt()
        valueVo.redisValue = ""

        var bytesKeys: List<ByteArray> = connection.hashCommands().hKeys(keyBytes)!!.toList()
        bytesKeys = if (bytesKeys.size <= 300) bytesKeys else bytesKeys.subList(0, 300)

        val bytesValues = connection.hashCommands().hMGet(keyBytes, *bytesKeys.toTypedArray<ByteArray>())

        val finalBytesKeys = bytesKeys
        val fieldVos = IntStream.range(0, bytesKeys.size)
            .mapToObj { i: Int ->
                val bytesKey = finalBytesKeys[i]
                val bytesValue = bytesValues!![i]
                val fieldVo: FieldVo = FieldVo().apply {
                    fieldValueSize = bytesValue.size
                }

                if (isJdkSerialize(bytesKey)) {
                    val pair = dealJdkDeserialize(bytesKey)
                    fieldVo.fieldKey = pair.left
                    fieldVo.fieldKeyClazz = pair.right
                    fieldVo.fieldKeyJdkSerialize = true
                } else {
                    fieldVo.fieldKey = String(bytesKey, StandardCharsets.UTF_8)
                    fieldVo.fieldKeyJdkSerialize = false
                }

                if (isJdkSerialize(bytesValue)) {
                    val pair = dealJdkDeserialize(bytesValue)
                    fieldVo.fieldValue = pair.left
                    fieldVo.fieldValueClazz = pair.right
                    fieldVo.fieldValueJdkSerialize = true
                } else {
                    fieldVo.fieldValue = String(bytesValue, StandardCharsets.UTF_8)
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

        val fieldVos = valueVo.fieldVos!!.stream()
            .filter { it: FieldVo -> it.fieldKey == commonRedisVo.fieldKey }
            .collect(Collectors.toList())

        val oldFieldVo = fieldVos[0]

        val b = saveField(
            connection, keyPair, commonRedisVo.fieldKey!!, commonRedisVo.fieldKeyJdkSerialize!!,
            commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, oldFieldVo
        )
        return if (b) 1L else 0L
    }

    private fun saveField(
        connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>,
        fieldKey: String, fieldKeyJdkSerialize: Boolean,
        fieldValue: String, fieldValueJdkSerialize: Boolean,
        oldFieldVo: FieldVo?
    ): Boolean {
        val keyBytes = keyPair.left

        val fieldKeyBytes: ByteArray
        val fieldValueBytes: ByteArray
        if (oldFieldVo == null) {
            if (fieldKeyJdkSerialize) {
                val pair = resolveVal(
                    fieldKey
                )
                val obj = convertValToObj(
                    pair.left, pair.right
                )
                fieldKeyBytes = serialize(obj!!)
            } else {
                fieldKeyBytes = fieldKey.toByteArray(StandardCharsets.UTF_8)
            }

            if (fieldValueJdkSerialize) {
                val pair = resolveVal(
                    fieldValue
                )
                val obj = convertValToObj(
                    pair.left, pair.right
                )
                fieldValueBytes = serialize(obj!!)
            } else {
                fieldValueBytes = fieldValue.toByteArray(StandardCharsets.UTF_8)
            }
        } else {
            if (oldFieldVo.fieldKeyJdkSerialize!!) {
                val obj = convertValToObj(fieldKey, oldFieldVo.fieldKeyClazz!!)
                fieldKeyBytes = serialize(obj!!)
            } else {
                fieldKeyBytes = fieldKey.toByteArray(StandardCharsets.UTF_8)
            }

            if (oldFieldVo.fieldValueJdkSerialize!!) {
                val obj = convertValToObj(fieldValue, oldFieldVo.fieldValueClazz!!)
                fieldValueBytes = serialize(obj!!)
            } else {
                fieldValueBytes = fieldValue.toByteArray(StandardCharsets.UTF_8)
            }
        }


        return connection.hashCommands().hSet(keyBytes, fieldKeyBytes, fieldValueBytes)!!
    }

    override fun addKey(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        saveField(
            connection, keyPair, commonRedisVo.fieldKey!!,
            commonRedisVo.fieldKeyJdkSerialize!!, commonRedisVo.redisValue!!,
            commonRedisVo.redisValueJdkSerialize!!, null
        )

        return getValue(connection, keyPair)
    }

    override fun renameKey(
        connection: RedisConnection, oldKeyPair: Pair<ByteArray, Class<*>>,
        keyPair: Pair<ByteArray, Class<*>>, commonRedisVo: CommonRedisVo
    ): ValueVo {
        val oldValueVo = getValue(connection, oldKeyPair)

        for (fieldVo in oldValueVo.fieldVos!!) {
            saveField(
                connection, keyPair, fieldVo.fieldKey!!, fieldVo.fieldKeyJdkSerialize!!, fieldVo.fieldValue!!,
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
        val keyBytes = keyPair.left
        val valueVo = getValue(connection, keyPair)
        val affect: Long?

        val fieldVos = valueVo.fieldVos!!.stream()
            .filter { it: FieldVo -> it.fieldKey == commonRedisVo.fieldKey }
            .collect(Collectors.toList())

        if (fieldVos.isNotEmpty() && fieldVos[0].fieldValueJdkSerialize!!) {
            val obj = convertValToObj(commonRedisVo.fieldKey, fieldVos[0].fieldKeyClazz!!)
            affect = connection.hashCommands().hDel(
                keyBytes, serialize(
                    obj!!
                )
            )
        } else {
            affect = connection.hashCommands()
                .hDel(keyBytes, commonRedisVo.fieldKey!!.toByteArray(StandardCharsets.UTF_8))
        }
        return affect!!
    }

    override fun addField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): String {
        return saveField(
            connection, keyPair, commonRedisVo.fieldKey!!, commonRedisVo.fieldKeyJdkSerialize!!,
            commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, null
        ).toString() + ""
    }

    override fun dataType(): DataType {
        return DataType.HASH
    }
}
