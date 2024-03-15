package org.javamaster.invocationlab.admin.redis.impl;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.AbstractRedisDataTypeService;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service("string")
public class StringRedisDataTypeService extends AbstractRedisDataTypeService {

    @Override
    public ValueVo getValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        ValueVo valueVo = createValueVo(connection, keyPair);

        byte[] bytesValue = connection.stringCommands().get(keyPair.getLeft());
        if (bytesValue == null) {
            return valueVo;
        }
        valueVo.setRedisValueSize(bytesValue.length);

        if (SerializationUtils.isJdkSerialize(bytesValue)) {
            Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(bytesValue);
            valueVo.setRedisValue(pair.getLeft());
            valueVo.setRedisValueClazz(pair.getRight());
            valueVo.setRedisValueJdkSerialize(true);
        } else {
            String value = new String(bytesValue, StandardCharsets.UTF_8);
            valueVo.setRedisValue(value);
            valueVo.setRedisValueJdkSerialize(false);
        }

        return valueVo;
    }

    @Override
    public Long saveValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo oldValueVo = getValue(connection, keyPair);

        Boolean b = saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(), oldValueVo);
        return b ? 1L : 0L;
    }

    private Boolean saveValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair, String valNew,
                              Boolean valNewJdkSerialize, ValueVo oldValueVo) {
        byte[] keyBytes = keyPair.getLeft();
        RedisStringCommands stringCommands = connection.stringCommands();

        byte[] bytesNewVal;
        if (oldValueVo == null) {
            if (valNewJdkSerialize) {
                Pair<String, Class<?>> pair = resolveVal(valNew);
                Object obj = SerializationUtils.convertValToObj(pair.getLeft(), pair.getRight());
                bytesNewVal = SerializationUtils.serialize(obj);
            } else {
                bytesNewVal = valNew.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            if (oldValueVo.getRedisValueJdkSerialize()) {
                Object obj = SerializationUtils.convertValToObj(valNew, oldValueVo.getRedisValueClazz());
                bytesNewVal = SerializationUtils.serialize(obj);
            } else {
                bytesNewVal = valNew.getBytes(StandardCharsets.UTF_8);
            }
        }

        return stringCommands.set(keyBytes, bytesNewVal);
    }

    @Override
    public ValueVo addKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(), null);

        return getValue(connection, keyPair);
    }

    @Override
    public ValueVo renameKey(RedisConnection connection, Pair<byte[], Class<?>> oldKeyPair,
                             Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        ValueVo oldValueVo = getValue(connection, oldKeyPair);

        saveValue(connection, keyPair, commonRedisVo.getRedisValue(), commonRedisVo.getRedisValueJdkSerialize(), oldValueVo);

        delKey(connection, oldKeyPair);

        return getValue(connection, keyPair);
    }

    @Override
    public Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String addField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataType dataType() {
        return DataType.STRING;
    }
}
