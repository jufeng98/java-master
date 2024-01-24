package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo;
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo;
import org.javamaster.invocationlab.admin.model.redis.Tree;
import org.javamaster.invocationlab.admin.model.redis.ValueVo;

import java.util.List;

/**
 * @author yudong
 */
public interface RedisService {
    String saveConnect(ConnectionVo connectionVoReq) throws Exception;

    List<Tree> listDb(String connectId) throws Exception;

    String pingConnect(ConnectionVo connectionVoReq) throws Exception;

    List<ConnectionVo> listConnects() throws Exception;

    List<Tree> listKeys(String connectId, Integer redisDbIndex, String pattern) throws Exception;

    ValueVo getValue(CommonRedisVo commonRedisVo) throws Exception;

    ValueVo renameKey(CommonRedisVo commonRedisVo) throws Exception;

    String delKey(CommonRedisVo commonRedisVo) throws Exception;

    ValueVo saveValue(CommonRedisVo commonRedisVo) throws Exception;

    String setNewTtl(CommonRedisVo commonRedisVo) throws Exception;

    ValueVo addKey(CommonRedisVo commonRedisVo) throws Exception;

    String delField(CommonRedisVo commonRedisVo) throws Exception;

    String addField(CommonRedisVo commonRedisVo) throws Exception;
}
