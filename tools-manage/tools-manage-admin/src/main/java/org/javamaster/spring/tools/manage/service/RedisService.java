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
    /**
     * 保存连接对象信息
     */
    String saveConnect(ConnectionVo connectionVoReq) throws Exception;

    /**
     * 获取特定连接下的db列表
     */
    List<Tree> listDb(String connectId) throws Exception;

    /**
     * 测试连接
     */
    String pingConnect(ConnectionVo connectionVoReq) throws Exception;

    /**
     * 获取连接列表
     */
    List<ConnectionVo> listConnects() throws Exception;

    /**
     * 获取特定连接下的key列表
     */
    List<Tree> listKeys(String connectId, Integer redisDbIndex, String pattern) throws Exception;

    /**
     * 获取value
     */
    ValueVo getValue(CommonVo commonVo) throws Exception;

    /**
     * 重命名key
     */
    ValueVo renameKey(CommonVo commonVo) throws Exception;

    /**
     * 删除key
     */
    String delKey(CommonVo commonVo) throws Exception;

    /**
     * 保存value
     */
    ValueVo saveValue(CommonVo commonVo) throws Exception;

    /**
     * 设置新的TTL
     */
    String setNewTtl(CommonVo commonVo) throws Exception;

    /**
     * 新增key
     */
    ValueVo addKey(CommonVo commonVo) throws Exception;

    /**
     * 删除field
     */
    String delField(CommonVo commonVo) throws Exception;

    /**
     * 新增field
     */
    String addField(CommonVo commonVo) throws Exception;
}
