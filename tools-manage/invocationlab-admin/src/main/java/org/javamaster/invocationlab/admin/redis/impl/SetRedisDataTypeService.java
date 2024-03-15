package org.javamaster.invocationlab.admin.redis.impl;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.FieldVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.AbstractRedisDataTypeService;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import lombok.val;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisSetCommands;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.util.SerializationUtils.serialize;

public class SetRedisDataTypeService extends AbstractRedisDataTypeService {

    @Override
    public ValueVo getValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        ValueVo valueVo = createValueVo(connection, keyPair);
        byte[] keyBytes = keyPair.getLeft();
        RedisSetCommands setCommands = connection.setCommands();

        Long len = setCommands.sCard(keyBytes);
        if (len == null) {
            return valueVo;
        }

        valueVo.setFieldCount(len.intValue());
        List<byte[]> bytesValues = setCommands.sRandMember(keyBytes, 300);
        val fieldVos = Objects.requireNonNull(bytesValues).stream()
                .map(bytes -> {
                    FieldVo fieldVo = FieldVo.builder()
                            .fieldValueSize(bytes.length)
                            .build();

                    if (SerializationUtils.isJdkSerialize(bytes)) {
                        Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(bytes);
                        fieldVo.setFieldValue(pair.getLeft());
                        fieldVo.setFieldValueClazz(pair.getRight());
                        fieldVo.setFieldValueJdkSerialize(true);
                    } else {
                        fieldVo.setFieldValue(new String(bytes, StandardCharsets.UTF_8));
                        fieldVo.setFieldValueJdkSerialize(false);
                    }
                    return fieldVo;
                })
                .collect(Collectors.toList());

        valueVo.setFieldVos(fieldVos);

        return valueVo;
    }

    @Override
    public Long saveValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo valueVo = getValue(connection, keyPair);

        FieldVo oldFieldVo = findFieldVo(valueVo.getFieldVos(), commonRedisVo.getOldRedisValue()).getLeft();

        return saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(),
                oldFieldVo);
    }

    private Long saveValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair, String valNew,
                           Boolean valNewJdkSerialize, FieldVo oldFieldVo) {
        byte[] keyBytes = keyPair.getLeft();
        RedisSetCommands setCommands = connection.setCommands();

        byte[] bytesNewVal;
        if (oldFieldVo == null) {
            if (valNewJdkSerialize) {
                Pair<String, Class<?>> pair = resolveVal(valNew);
                Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
                bytesNewVal = serialize(obj);
            } else {
                bytesNewVal = valNew.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            Class<?> clazz = oldFieldVo.getFieldValueClazz();
            String valOld = oldFieldVo.getFieldValue();
            byte[] bytesOldVal;
            if (oldFieldVo.getFieldValueJdkSerialize()) {
                Object oldObj = SerializationUtils.convertValToObj(valOld, clazz);
                bytesOldVal = SerializationUtils.serialize(oldObj);

                Object obj = SerializationUtils.convertValToObj(valNew, clazz);
                bytesNewVal = serialize(obj);
            } else {
                bytesOldVal = valOld.getBytes(StandardCharsets.UTF_8);

                bytesNewVal = valNew.getBytes(StandardCharsets.UTF_8);
            }

            setCommands.sRem(keyBytes, bytesOldVal);
        }
        return setCommands.sAdd(keyBytes, bytesNewVal);
    }

    @Override
    public ValueVo addKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(),
                null);

        return getValue(connection, keyPair);
    }

    @Override
    public ValueVo renameKey(RedisConnection connection, Pair<byte[], Class<?>> oldKeyPair, Pair<byte[], Class<?>> keyPair,
                             CommonRedisVo commonRedisVo) {
        ValueVo oldValueVo = getValue(connection, oldKeyPair);

        for (FieldVo fieldVo : oldValueVo.getFieldVos()) {
            saveValue(connection, keyPair, fieldVo.getFieldValue(), commonRedisVo.getRedisValueJdkSerialize(),
                    null);
        }

        delKey(connection, oldKeyPair);

        return getValue(connection, keyPair);
    }

    private Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, FieldVo fieldVo) {
        byte[] keyBytes = keyPair.getLeft();

        String val = fieldVo.getFieldValue();
        byte[] bytesVal;
        if (fieldVo.getFieldValueJdkSerialize()) {
            Object obj = SerializationUtils.convertValToObj(val, fieldVo.getFieldValueClazz());
            bytesVal = serialize(obj);
        } else {
            bytesVal = val.getBytes(StandardCharsets.UTF_8);
        }
        return connection.setCommands().sRem(keyBytes, bytesVal);
    }

    @Override
    public Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo valueVo = getValue(connection, keyPair);

        FieldVo oldFieldVo = findFieldVo(valueVo.getFieldVos(), commonRedisVo.getRedisValue()).getLeft();

        return delField(connection, keyPair, oldFieldVo);
    }

    @Override
    public String addField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        return saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(),
                null) + "";
    }

    @Override
    public DataType dataType() {
        return DataType.SET;
    }
}
