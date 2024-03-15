package org.javamaster.invocationlab.admin.redis;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.impl.HashRedisDataTypeService;
import org.javamaster.invocationlab.admin.redis.impl.ListRedisDataTypeService;
import org.javamaster.invocationlab.admin.redis.impl.SetRedisDataTypeService;
import org.javamaster.invocationlab.admin.redis.impl.StringRedisDataTypeService;
import org.javamaster.invocationlab.admin.redis.impl.ZSetRedisDataTypeService;
import org.javamaster.invocationlab.admin.service.Pair;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;

import java.util.HashMap;
import java.util.Map;

public interface RedisDataTypeService {
    Map<DataType, RedisDataTypeService> MAP = new HashMap<DataType, RedisDataTypeService>() {
        {
            put(DataType.STRING, new StringRedisDataTypeService());
            put(DataType.HASH, new HashRedisDataTypeService());
            put(DataType.LIST, new ListRedisDataTypeService());
            put(DataType.SET, new SetRedisDataTypeService());
            put(DataType.ZSET, new ZSetRedisDataTypeService());
        }
    };

    static RedisDataTypeService getInstance(DataType dataType) {
        return MAP.get(dataType);
    }

    ValueVo getValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair);

    Long saveValue(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo);

    ValueVo addKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo);

    Long delKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair);

    ValueVo renameKey(RedisConnection connection, Pair<byte[], Class<?>> oldKeyPair, Pair<byte[], Class<?>> keyPair,
                      CommonRedisVo commonRedisVo);

    Long delField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo);

    String addField(RedisConnection connection, Pair<byte[], Class<?>> keyPair, CommonRedisVo commonRedisVo);

    Boolean setTtlIfNecessary(RedisConnection connection, Pair<byte[], Class<?>> keyPair, Long redisKeyTtl);

    DataType dataType();
}
