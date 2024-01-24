package org.javamaster.invocationlab.admin.redis.impl;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.FieldVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.AbstractRedisStrategy;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import lombok.val;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ZSetRedisStrategy extends AbstractRedisStrategy {

    @Override
    public ValueVo getValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        ValueVo valueVo = createValueVo(connection, keyPair);

        byte[] keyBytes = keyPair.getLeft();
        Long len = connection.zSetCommands().zCard(keyBytes);
        if (len == null) {
            return valueVo;
        }
        valueVo.setFieldCount(len.intValue());
        valueVo.setRedisValue("");

        Set<RedisZSetCommands.Tuple> tuples = connection.zSetCommands().zRangeWithScores(keyBytes, 0, 300);
        val fieldVos = Objects.requireNonNull(tuples).stream()
                .map(tuple -> {
                    FieldVo fieldVo = FieldVo.builder()
                            .fieldScore(tuple.getScore())
                            .fieldValueSize(tuple.getValue().length)
                            .build();

                    if (SerializationUtils.isJdkSerialize(tuple.getValue())) {
                        Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(tuple.getValue());
                        fieldVo.setFieldValue(pair.getLeft());
                        fieldVo.setFieldValueClazz(pair.getRight());
                        fieldVo.setFieldValueJdkSerialize(true);
                    } else {
                        fieldVo.setFieldValue(new String(tuple.getValue(), StandardCharsets.UTF_8));
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

        Boolean b = saveField(connection, keyPair, oldFieldVo.getFieldScore(), commonRedisVo.getRedisValue(),
                commonRedisVo.getRedisValueJdkSerialize(), oldFieldVo);
        return b ? 1L : 0L;
    }

    @Override
    public ValueVo addKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        saveField(connection, keyPair, commonRedisVo.getScore(),
                commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(), null);

        return getValue(connection, keyPair);
    }

    @Override
    public ValueVo renameKey(RedisConnection connection, Pair<byte[], Class<?>> oldKeyPair, Pair<byte[], Class<?>> keyPair,
                             CommonRedisVo commonRedisVo) {
        ValueVo oldValueVo = getValue(connection, oldKeyPair);

        for (FieldVo fieldVo : oldValueVo.getFieldVos()) {
            saveField(connection, keyPair, fieldVo.getFieldScore(), fieldVo.getFieldValue(),
                    fieldVo.getFieldValueJdkSerialize(), null);
        }

        delKey(connection, oldKeyPair);

        return getValue(connection, keyPair);
    }

    @Override
    public Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo valueVo = getValue(connection, keyPair);

        FieldVo oldFieldVo = findFieldVo(valueVo.getFieldVos(), commonRedisVo.getRedisValue()).getLeft();

        return delField(connection, keyPair, oldFieldVo);
    }

    private Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, FieldVo oldFieldVo) {
        byte[] keyBytes = keyPair.getLeft();

        byte[] bytesNewVal;
        if (oldFieldVo.getFieldValueJdkSerialize()) {
            Object obj = SerializationUtils.convertValToObj(oldFieldVo.getFieldValue(), oldFieldVo.getFieldValueClazz());
            bytesNewVal = SerializationUtils.serialize(obj);
        } else {
            bytesNewVal = oldFieldVo.getFieldValue().getBytes(StandardCharsets.UTF_8);
        }
        return connection.zSetCommands().zRem(keyBytes, bytesNewVal);
    }

    private Boolean saveField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, Double score, String fieldValue,
                              Boolean fieldValueJdkSerialize, @Nullable FieldVo oldFieldVo) {
        byte[] keyBytes = keyPair.getLeft();
        RedisZSetCommands redisZSetCommands = connection.zSetCommands();

        byte[] bytesNewVal;
        if (oldFieldVo == null) {
            if (fieldValueJdkSerialize) {
                Pair<String, Class<?>> pair = resolveVal(fieldValue);
                Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
                bytesNewVal = SerializationUtils.serialize(obj);
            } else {
                bytesNewVal = fieldValue.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            delField(connection, keyPair, oldFieldVo);

            if (oldFieldVo.getFieldValueJdkSerialize()) {
                Object obj = SerializationUtils.convertValToObj(fieldValue, oldFieldVo.getFieldValueClazz());
                bytesNewVal = SerializationUtils.serialize(obj);
            } else {
                bytesNewVal = fieldValue.getBytes(StandardCharsets.UTF_8);
            }

        }
        return redisZSetCommands.zAdd(keyBytes, score, bytesNewVal);
    }

    @Override
    public String addField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        return saveField(connection, keyPair, commonRedisVo.getScore(), commonRedisVo.getRedisValue(),
                commonRedisVo.getRedisValueJdkSerialize(), null) + "";
    }

    @Override
    public DataType dataType() {
        return DataType.ZSET;
    }
}
