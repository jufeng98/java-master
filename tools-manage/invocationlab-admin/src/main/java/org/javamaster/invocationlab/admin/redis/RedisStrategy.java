package org.javamaster.invocationlab.admin.redis;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.redis.impl.HashRedisStrategy;
import org.javamaster.invocationlab.admin.redis.impl.ListRedisStrategy;
import org.javamaster.invocationlab.admin.redis.impl.SetRedisStrategy;
import org.javamaster.invocationlab.admin.redis.impl.StringRedisStrategy;
import org.javamaster.invocationlab.admin.redis.impl.ZSetRedisStrategy;
import org.javamaster.invocationlab.admin.service.Pair;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;

import java.util.HashMap;
import java.util.Map;

public interface RedisStrategy {
    Map<DataType, RedisStrategy> MAP = new HashMap<DataType, RedisStrategy>() {
        {
            put(DataType.STRING, new StringRedisStrategy());
            put(DataType.HASH, new HashRedisStrategy());
            put(DataType.LIST, new ListRedisStrategy());
            put(DataType.SET, new SetRedisStrategy());
            put(DataType.ZSET, new ZSetRedisStrategy());
        }
    };

    static RedisStrategy getInstance(DataType dataType) {
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
