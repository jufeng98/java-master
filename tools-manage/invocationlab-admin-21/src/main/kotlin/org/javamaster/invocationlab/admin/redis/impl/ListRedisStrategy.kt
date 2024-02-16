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

class ListRedisStrategy : AbstractRedisStrategy() {
    override fun getValue(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo {
        val valueVo = createValueVo(connection, keyPair)
        val listCommands = connection.listCommands()

        val keyBytes = keyPair.left
        var len = listCommands.lLen(keyBytes) ?: return valueVo

        valueVo.fieldCount = len.toInt()
        len = if (len <= 300) len else 300
        val bytesValues = listCommands.lRange(keyBytes, 0, len)
        val fieldVos = bytesValues!!.stream()
            .map { bytes: ByteArray ->
                val fieldVo: FieldVo = FieldVo().apply {
                    fieldValueSize = bytesValues.size
                }
                if (isJdkSerialize(bytes)) {
                    val pair = dealJdkDeserialize(bytes)
                    fieldVo.fieldValue = pair.left
                    fieldVo.fieldValueClazz = pair.right
                    fieldVo.fieldValueJdkSerialize = true
                } else {
                    fieldVo.fieldValue = String(bytes, StandardCharsets.UTF_8)
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

        val pair = findFieldVo(valueVo.fieldVos!!, commonRedisVo.oldRedisValue!!)
        val fieldVoOld = pair.left
        val index = pair.right

        return saveValue(
            connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, fieldVoOld,
            index
        )
    }

    private fun saveValue(
        connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, valNew: String,
        valNewJdkSerialize: Boolean,
        oldFieldVo: FieldVo?, oldIndex: Int?
    ): Long {
        val keyBytes = keyPair.left
        val listCommands = connection.listCommands()

        val bytesNewVal: ByteArray
        if (oldFieldVo == null) {
            if (valNewJdkSerialize) {
                val pair = resolveVal(valNew)
                val obj = convertValToObj(pair.left, pair.right)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = valNew.toByteArray(StandardCharsets.UTF_8)
            }
            return listCommands.rPush(keyBytes, bytesNewVal)!!
        } else {
            if (oldFieldVo.fieldValueJdkSerialize!!) {
                val obj = convertValToObj(valNew, oldFieldVo.fieldValueClazz!!)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = valNew.toByteArray(StandardCharsets.UTF_8)
            }
            listCommands.lSet(keyBytes, oldIndex!!.toLong(), bytesNewVal)
        }
        return 1L
    }

    override fun addKey(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        saveValue(connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!, null, null)

        return getValue(connection, keyPair)
    }

    override fun renameKey(
        connection: RedisConnection, oldKeyPair: Pair<ByteArray, Class<*>>,
        keyPair: Pair<ByteArray, Class<*>>, commonRedisVo: CommonRedisVo
    ): ValueVo {
        val oldValueVo = getValue(connection, oldKeyPair)

        for (fieldVo in oldValueVo.fieldVos!!) {
            saveValue(connection, keyPair, fieldVo.fieldValue!!, fieldVo.fieldValueJdkSerialize!!, null, null)
        }

        delKey(connection, oldKeyPair)

        return getValue(connection, keyPair)
    }

    private fun delField(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, fieldVo: FieldVo): Long {
        val keyBytes = keyPair.left

        val fieldValue = fieldVo.fieldValue

        val bytesVal: ByteArray
        if (fieldVo.fieldValueJdkSerialize!!) {
            val obj = convertValToObj(fieldValue, fieldVo.fieldValueClazz!!)
            bytesVal = serialize(obj!!)
        } else {
            bytesVal = fieldValue!!.toByteArray(StandardCharsets.UTF_8)
        }
        return connection.listCommands().lRem(keyBytes, 1, bytesVal)!!
    }

    override fun delField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): Long {
        val valueVo = getValue(connection, keyPair)

        val pair = findFieldVo(valueVo.fieldVos!!, commonRedisVo.redisValue!!)

        return delField(connection, keyPair, pair.left!!)
    }

    override fun addField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): String {
        return saveValue(
            connection,
            keyPair,
            commonRedisVo.redisValue!!,
            commonRedisVo.redisValueJdkSerialize!!,
            null,
            null
        ).toString() + ""
    }

    override fun dataType(): DataType {
        return DataType.LIST
    }
}
