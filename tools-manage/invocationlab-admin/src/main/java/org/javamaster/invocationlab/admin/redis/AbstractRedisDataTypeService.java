package org.javamaster.invocationlab.admin.redis;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.model.redis.FieldVo;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.SerializationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;


@Slf4j
public abstract class AbstractRedisDataTypeService implements RedisDataTypeService {

    public ValueVo createValueVo(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        byte[] keyBytes = keyPair.getLeft();
        Class<?> clazz = keyPair.getRight();

        Long ttl = connection.keyCommands().ttl(keyBytes);

        ValueVo valueVo = ValueVo.builder()
                .redisKeyTtl(ttl)
                .redisKeyType(dataType().code())
                .redisKeyClazz(clazz)
                .fieldCount(0)
                .fieldVos(Collections.emptyList())
                .build();

        if (SerializationUtils.isJdkSerialize(keyBytes)) {
            Pair<String, Class<?>> pair = SerializationUtils.dealJdkDeserialize(keyBytes);
            valueVo.setRedisKey(pair.getLeft());
            valueVo.setRedisKeyJdkSerialize(true);
            valueVo.setRedisKeyBase64(Base64Utils.encodeToString(keyBytes));
        } else {
            valueVo.setRedisKey(new String(keyBytes, StandardCharsets.UTF_8));
            valueVo.setRedisKeyJdkSerialize(false);
        }
        return valueVo;
    }

    @Override
    public Boolean setTtlIfNecessary(RedisConnection connection, Pair<byte[], Class<?>> keyPair, Long redisKeyTtl) {
        byte[] keyBytes = keyPair.getLeft();
        if (redisKeyTtl == null) {
            return false;
        }
        Boolean persist;
        if (-1 == redisKeyTtl) {
            persist = connection.keyCommands().persist(keyBytes);
        } else {
            persist = connection.keyCommands().expire(keyBytes, redisKeyTtl);
        }
        log.info("设置persist结果:{}", persist);
        return persist;
    }

    @Override
    public Long delKey(RedisConnection connection, Pair<byte[], Class<?>> keyPair) {
        byte[] keyBytes = keyPair.getLeft();
        return connection.keyCommands().del(keyBytes);
    }

    protected Pair<FieldVo, Integer> findFieldVo(List<FieldVo> fieldVos, String fieldVal) {
        int index = -1;
        FieldVo fieldVoOld = null;
        for (int i = 0; i < fieldVos.size(); i++) {
            FieldVo fieldVo = fieldVos.get(i);
            if (fieldVo.getFieldValue().equals(fieldVal)) {
                index = i;
                fieldVoOld = fieldVo;
                break;
            }
        }
        if (fieldVoOld == null) {
            throw new BizException("field:" + fieldVal + " 已不存在!!!");
        }
        return Pair.of(fieldVoOld, index);
    }

    public Pair<String, Class<?>> resolveVal(String s) {
        if (JsonUtils.isJsonObjOrArray(s)) {
            return Pair.of(s, Void.class);
        }
        return RedisHelper.resolveVal(s);
    }
}
