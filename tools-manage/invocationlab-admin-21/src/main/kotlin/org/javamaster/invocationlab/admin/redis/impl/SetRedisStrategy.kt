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

class SetRedisStrategy : AbstractRedisStrategy() {
    override fun getValue(connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>): ValueVo {
        val valueVo = createValueVo(connection, keyPair)
        val keyBytes = keyPair.left
        val setCommands = connection.setCommands()

        val len = setCommands.sCard(keyBytes) ?: return valueVo

        valueVo.fieldCount = len.toInt()
        val bytesValues = setCommands.sRandMember(keyBytes, 300)
        val fieldVos = bytesValues!!.stream()
            .map { bytes: ByteArray ->
                val fieldVo: FieldVo = FieldVo().apply {
                    fieldValueSize = bytes.size
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

        val oldFieldVo = findFieldVo(valueVo.fieldVos!!, commonRedisVo.oldRedisValue!!).left

        return saveValue(
            connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!,
            oldFieldVo
        )
    }

    private fun saveValue(
        connection: RedisConnection, keyPair: Pair<ByteArray, Class<*>>, valNew: String,
        valNewJdkSerialize: Boolean, oldFieldVo: FieldVo?
    ): Long {
        val keyBytes = keyPair.left
        val setCommands = connection.setCommands()

        val bytesNewVal: ByteArray
        if (oldFieldVo == null) {
            if (valNewJdkSerialize) {
                val pair = resolveVal(valNew)
                val obj = convertValToObj(pair.left, pair.right)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesNewVal = valNew.toByteArray(StandardCharsets.UTF_8)
            }
        } else {
            val clazz = oldFieldVo.fieldValueClazz
            val valOld = oldFieldVo.fieldValue
            val bytesOldVal: ByteArray
            if (oldFieldVo.fieldValueJdkSerialize!!) {
                val oldObj = convertValToObj(valOld, clazz!!)
                bytesOldVal = serialize(oldObj!!)

                val obj = convertValToObj(valNew, clazz)
                bytesNewVal = serialize(obj!!)
            } else {
                bytesOldVal = valOld!!.toByteArray(StandardCharsets.UTF_8)

                bytesNewVal = valNew.toByteArray(StandardCharsets.UTF_8)
            }

            setCommands.sRem(keyBytes, bytesOldVal)
        }
        return setCommands.sAdd(keyBytes, bytesNewVal)!!
    }

    override fun addKey(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        saveValue(
            connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!,
            null
        )

        return getValue(connection, keyPair)
    }

    override fun renameKey(
        connection: RedisConnection, oldKeyPair: Pair<ByteArray, Class<*>>, keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): ValueVo {
        val oldValueVo = getValue(connection, oldKeyPair)

        for (fieldVo in oldValueVo.fieldVos!!) {
            saveValue(
                connection, keyPair, fieldVo.fieldValue!!, commonRedisVo.redisValueJdkSerialize!!,
                null
            )
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
        return connection.setCommands().sRem(keyBytes, bytesVal)!!
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

    override fun addField(
        connection: RedisConnection,
        keyPair: Pair<ByteArray, Class<*>>,
        commonRedisVo: CommonRedisVo
    ): String {
        return saveValue(
            connection, keyPair, commonRedisVo.redisValue!!, commonRedisVo.redisValueJdkSerialize!!,
            null
        ).toString() + ""
    }

    override fun dataType(): DataType {
        return DataType.SET
    }
}
