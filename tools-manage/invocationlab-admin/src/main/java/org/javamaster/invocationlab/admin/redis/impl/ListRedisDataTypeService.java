package org.javamaster.invocationlab.admin.redis.impl;

import lombok.val;
import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.FieldVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.AbstractRedisDataTypeService;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.util.SerializationUtils.serialize;

@Service("list")
public class ListRedisDataTypeService extends AbstractRedisDataTypeService {

    @Override
    public ValueVo getValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        ValueVo valueVo = createValueVo(connection, keyPair);
        RedisListCommands listCommands = connection.listCommands();

        byte[] keyBytes = keyPair.getLeft();
        Long len = listCommands.lLen(keyBytes);
        if (len == null) {
            return valueVo;
        }

        valueVo.setFieldCount(len.intValue());
        len = len <= 300 ? len : 300;
        List<byte[]> bytesValues = listCommands.lRange(keyBytes, 0, len);
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

        Pair<FieldVo, Integer> pair = findFieldVo(valueVo.getFieldVos(), commonRedisVo.getOldRedisValue());
        FieldVo fieldVoOld = pair.getLeft();
        int index = pair.getRight();

        return saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(),
                fieldVoOld, index);
    }

    private Long saveValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair, String valNew,
                           Boolean valNewJdkSerialize, FieldVo oldFieldVo, Integer oldIndex) {
        byte[] keyBytes = keyPair.getLeft();
        RedisListCommands listCommands = connection.listCommands();

        byte[] bytesNewVal;
        if (oldFieldVo == null) {
            if (valNewJdkSerialize) {
                Pair<String, Class<?>> pair = resolveVal(valNew);
                Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
                bytesNewVal = serialize(obj);
            } else {
                bytesNewVal = valNew.getBytes(StandardCharsets.UTF_8);
            }
            return listCommands.rPush(keyBytes, bytesNewVal);
        } else {
            if (oldFieldVo.getFieldValueJdkSerialize()) {
                Object obj = SerializationUtils.convertValToObj(valNew, oldFieldVo.getFieldValueClazz());
                bytesNewVal = serialize(obj);
            } else {
                bytesNewVal = valNew.getBytes(StandardCharsets.UTF_8);
            }
            listCommands.lSet(keyBytes, oldIndex, bytesNewVal);
            return 1L;
        }
    }

    @Override
    public ValueVo addKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(),
                null, null);

        return getValue(connection, keyPair);
    }

    @Override
    public ValueVo renameKey(RedisConnection connection, Pair<byte[], Class<?>> oldKeyPair,
                             Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo oldValueVo = getValue(connection, oldKeyPair);

        for (FieldVo fieldVo : oldValueVo.getFieldVos()) {
            saveValue(connection, keyPair, fieldVo.getFieldValue(), fieldVo.getFieldValueJdkSerialize(),
                    null, null);
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
        return connection.listCommands().lRem(keyBytes, 1, bytesVal);
    }

    @Override
    public Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo valueVo = getValue(connection, keyPair);

        Pair<FieldVo, Integer> pair = findFieldVo(valueVo.getFieldVos(), commonRedisVo.getRedisValue());

        return delField(connection, keyPair, pair.getLeft());
    }

    @Override
    public String addField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        return saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(),
                null, null) + "";
    }

    @Override
    public DataType dataType() {
        return DataType.LIST;
    }
}
