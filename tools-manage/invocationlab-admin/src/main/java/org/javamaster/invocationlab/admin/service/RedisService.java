package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.model.redis.CommonVo;
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

    ValueVo getValue(CommonVo commonVo) throws Exception;

    ValueVo renameKey(CommonVo commonVo) throws Exception;

    String delKey(CommonVo commonVo) throws Exception;

    ValueVo saveValue(CommonVo commonVo) throws Exception;

    String setNewTtl(CommonVo commonVo) throws Exception;

    ValueVo addKey(CommonVo commonVo) throws Exception;

    String delField(CommonVo commonVo) throws Exception;

    String addField(CommonVo commonVo) throws Exception;
}
