package org.javamaster.invocationlab.admin.redis;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.service.Pair;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;

public interface RedisDataTypeService {
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
