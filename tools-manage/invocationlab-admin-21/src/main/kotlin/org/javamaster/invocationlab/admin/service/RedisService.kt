package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo
import org.javamaster.invocationlab.admin.model.redis.Tree
import org.javamaster.invocationlab.admin.model.redis.ValueVo

/**
 * @author yudong
 */
interface RedisService {

    fun saveConnect(connectionVoReq: ConnectionVo): String

    fun listDb(connectId: String): List<Tree>

    fun pingConnect(connectionVoReq: ConnectionVo): String

    fun listConnects(): List<ConnectionVo>

    fun listKeys(connectId: String, redisDbIndex: Int, pattern: String): List<Tree>

    fun getValue(commonRedisVo: CommonRedisVo): ValueVo

    fun renameKey(commonRedisVo: CommonRedisVo): ValueVo

    fun delKey(commonRedisVo: CommonRedisVo): String

    fun saveValue(commonRedisVo: CommonRedisVo): ValueVo

    fun setNewTtl(commonRedisVo: CommonRedisVo): String

    fun addKey(commonRedisVo: CommonRedisVo): ValueVo

    fun delField(commonRedisVo: CommonRedisVo): String

    fun addField(commonRedisVo: CommonRedisVo): String
}
