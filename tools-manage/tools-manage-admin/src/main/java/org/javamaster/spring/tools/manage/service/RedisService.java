package org.javamaster.spring.tools.manage.service;

import org.javamaster.spring.tools.manage.model.CommonVo;
import org.javamaster.spring.tools.manage.model.ConnectionVo;
import org.javamaster.spring.tools.manage.model.Tree;
import org.javamaster.spring.tools.manage.model.ValueVo;

import java.util.List;

/**
 * @author yudong
 * @date 2023.3.3
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
