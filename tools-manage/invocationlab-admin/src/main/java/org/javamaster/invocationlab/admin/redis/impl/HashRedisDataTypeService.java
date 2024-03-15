package org.javamaster.invocationlab.admin.redis.impl;

import com.google.common.collect.Lists;
import lombok.val;
import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.FieldVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.AbstractRedisDataTypeService;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.javamaster.invocationlab.admin.util.SerializationUtils.serialize;

@Service("hash")
public class HashRedisDataTypeService extends AbstractRedisDataTypeService {

    @Override
    public ValueVo getValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        ValueVo valueVo = createValueVo(connection, keyPair);

        byte[] keyBytes = keyPair.getLeft();
        Long fieldCount = connection.hashCommands().hLen(keyBytes);
        if (fieldCount == null) {
            return valueVo;
        }
        valueVo.setFieldCount(fieldCount.intValue());
        valueVo.setRedisValue("");

        @SuppressWarnings("DataFlowIssue")
        List<byte[]> bytesKeys = Lists.newArrayList(connection.hashCommands().hKeys(keyBytes));
        bytesKeys = bytesKeys.size() <= 300 ? bytesKeys : bytesKeys.subList(0, 300);

        List<byte[]> bytesValues = connection.hashCommands().hMGet(keyBytes, bytesKeys.toArray(new byte[0][]));
        Objects.requireNonNull(bytesValues);

        List<byte[]> finalBytesKeys = bytesKeys;
        val fieldVos = IntStream.range(0, bytesKeys.size())
                .mapToObj(i -> {
                    byte[] bytesKey = finalBytesKeys.get(i);
                    byte[] bytesValue = bytesValues.get(i);
                    FieldVo fieldVo = FieldVo.builder()
                            .fieldValueSize(bytesValue.length)
                            .build();

                    if (SerializationUtils.isJdkSerialize(bytesKey)) {
                        Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(bytesKey);
                        fieldVo.setFieldKey(pair.getLeft());
                        fieldVo.setFieldKeyClazz(pair.getRight());
                        fieldVo.setFieldKeyJdkSerialize(true);
                    } else {
                        fieldVo.setFieldKey(new String(bytesKey, StandardCharsets.UTF_8));
                        fieldVo.setFieldKeyJdkSerialize(false);
                    }

                    if (SerializationUtils.isJdkSerialize(bytesValue)) {
                        Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(bytesValue);
                        fieldVo.setFieldValue(pair.getLeft());
                        fieldVo.setFieldValueClazz(pair.getRight());
                        fieldVo.setFieldValueJdkSerialize(true);
                    } else {
                        fieldVo.setFieldValue(new String(bytesValue, StandardCharsets.UTF_8));
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

        List<FieldVo> fieldVos = valueVo.getFieldVos().stream()
                .filter(it -> it.getFieldKey().equals(commonRedisVo.getFieldKey()))
                .collect(Collectors.toList());

        FieldVo oldFieldVo = fieldVos.get(0);

        Boolean b = saveField(connection, keyPair, commonRedisVo.getFieldKey(), commonRedisVo.getFieldKeyJdkSerialize(),
                commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(), oldFieldVo);
        return b ? 1L : 0L;
    }

    public Boolean saveField(RedisConnection connection, Pair<byte[], Class<?>> keyPair,
                             String fieldKey, Boolean fieldKeyJdkSerialize,
                             String fieldValue, Boolean fieldValueJdkSerialize,
                             @Nullable FieldVo oldFieldVo) {
        byte[] keyBytes = keyPair.getLeft();

        byte[] fieldKeyBytes;
        byte[] fieldValueBytes;
        if (oldFieldVo == null) {
            if (fieldKeyJdkSerialize) {
                Pair<String, Class<?>> pair = resolveVal(fieldKey);
                Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
                fieldKeyBytes = serialize(obj);
            } else {
                fieldKeyBytes = fieldKey.getBytes(StandardCharsets.UTF_8);
            }

            if (fieldValueJdkSerialize) {
                Pair<String, Class<?>> pair = resolveVal(fieldValue);
                Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
                fieldValueBytes = serialize(obj);
            } else {
                fieldValueBytes = fieldKey.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            if (oldFieldVo.getFieldKeyJdkSerialize()) {
                Object obj = SerializationUtils.convertValToObj(fieldKey, oldFieldVo.getFieldKeyClazz());
                fieldKeyBytes = SerializationUtils.serialize(obj);
            } else {
                fieldKeyBytes = fieldKey.getBytes(StandardCharsets.UTF_8);
            }

            if (oldFieldVo.getFieldValueJdkSerialize()) {
                Object obj = SerializationUtils.convertValToObj(fieldValue, oldFieldVo.getFieldValueClazz());
                fieldValueBytes = SerializationUtils.serialize(obj);
            } else {
                fieldValueBytes = fieldValue.getBytes(StandardCharsets.UTF_8);
            }
        }


        return connection.hashCommands().hSet(keyBytes, fieldKeyBytes, fieldValueBytes);
    }

    @Override
    public ValueVo addKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        saveField(connection, keyPair, commonRedisVo.getFieldKey(),
                commonRedisVo.getFieldKeyJdkSerialize(), commonRedisVo.getRedisValue(),
                commonRedisVo.getRedisValueJdkSerialize(), null);

        return getValue(connection, keyPair);
    }

    @Override
    public ValueVo renameKey(RedisConnection connection, Pair<byte[], Class<?>> oldKeyPair,
                             Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo oldValueVo = getValue(connection, oldKeyPair);

        for (FieldVo fieldVo : oldValueVo.getFieldVos()) {
            saveField(connection, keyPair, fieldVo.getFieldKey(), fieldVo.getFieldKeyJdkSerialize(), fieldVo.getFieldValue(),
                    fieldVo.getFieldValueJdkSerialize(), null);
        }

        delKey(connection, oldKeyPair);

        return getValue(connection, keyPair);
    }

    @Override
    public Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        byte[] keyBytes = keyPair.getLeft();
        ValueVo valueVo = getValue(connection, keyPair);
        Long affect;

        List<FieldVo> fieldVos = valueVo.getFieldVos().stream()
                .filter(it -> it.getFieldKey().equals(commonRedisVo.getFieldKey()))
                .collect(Collectors.toList());

        if (!fieldVos.isEmpty() && fieldVos.get(0).getFieldValueJdkSerialize()) {
            Object obj = SerializationUtils.convertValToObj(commonRedisVo.getFieldKey(), fieldVos.get(0).getFieldKeyClazz());
            affect = connection.hashCommands().hDel(keyBytes, SerializationUtils.serialize(obj));
        } else {
            affect = connection.hashCommands().hDel(keyBytes, commonRedisVo.getFieldKey().getBytes(StandardCharsets.UTF_8));
        }
        return affect;
    }

    @Override
    public String addField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        return saveField(connection, keyPair, commonRedisVo.getFieldKey(), commonRedisVo.getFieldKeyJdkSerialize(),
                commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(), null) + "";
    }

    @Override
    public DataType dataType() {
        return DataType.HASH;
    }
}
