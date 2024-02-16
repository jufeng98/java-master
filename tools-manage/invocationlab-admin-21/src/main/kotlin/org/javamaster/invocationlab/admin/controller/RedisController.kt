package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto.Companion.success
import org.javamaster.invocationlab.admin.model.redis.CommonRedisVo
import org.javamaster.invocationlab.admin.model.redis.ConnectionVo
import org.javamaster.invocationlab.admin.model.redis.Tree
import org.javamaster.invocationlab.admin.model.redis.ValueVo
import org.javamaster.invocationlab.admin.service.RedisService
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 * @author yudong
 */
@RestController
@RequestMapping("/redis")
class RedisController {
    @Autowired
    private lateinit var redisService: RedisService

    @RequestMapping(value = ["/pingConnect"], method = [RequestMethod.GET, RequestMethod.POST])
    fun pingConnect(@RequestBody connectionVoReq: ConnectionVo): WebApiRspDto<String> {
        return success(redisService.pingConnect(connectionVoReq))
    }

    @RequestMapping(value = ["/saveConnect"], method = [RequestMethod.GET, RequestMethod.POST])
    fun saveConnect(@RequestBody connectionVoReq: ConnectionVo): WebApiRspDto<String> {
        return success(redisService.saveConnect(connectionVoReq))
    }

    @RequestMapping(value = ["/listConnects"], method = [RequestMethod.GET, RequestMethod.POST])
    fun listConnects(): WebApiRspDto<List<ConnectionVo>> {
        return success(redisService.listConnects())
    }

    @RequestMapping(value = ["/listDb/{connectId}"], method = [RequestMethod.GET, RequestMethod.POST])
    fun listDb(@PathVariable connectId: String): WebApiRspDto<List<Tree>> {
        return success(
            redisService.listDb(connectId)
        )
    }

    @RequestMapping(value = ["/listKeys/{connectId}/{redisDbIndex}"], method = [RequestMethod.GET, RequestMethod.POST])
    fun listKeys(
        @PathVariable connectId: String, @PathVariable redisDbIndex: Int,
        @RequestParam pattern: String
    ): WebApiRspDto<List<Tree>> {
        return success(
            redisService.listKeys(connectId, redisDbIndex, pattern)
        )
    }

    @RequestMapping(value = ["/getValue"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getValue(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<ValueVo> {
        return success(redisService.getValue(commonRedisVo))
    }

    @RequestMapping(value = ["/saveValue"], method = [RequestMethod.GET, RequestMethod.POST])
    fun saveValue(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<ValueVo> {
        return success(redisService.saveValue(commonRedisVo))
    }

    @RequestMapping(value = ["/setNewTtl"], method = [RequestMethod.GET, RequestMethod.POST])
    fun setNewTtl(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<String> {
        return success(redisService.setNewTtl(commonRedisVo))
    }

    @RequestMapping(value = ["/addKey"], method = [RequestMethod.GET, RequestMethod.POST])
    fun addKey(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<ValueVo> {
        return success(redisService.addKey(commonRedisVo))
    }

    @RequestMapping(value = ["/delKey"], method = [RequestMethod.GET, RequestMethod.POST])
    fun delKey(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<String> {
        return success(redisService.delKey(commonRedisVo))
    }

    @RequestMapping(value = ["/renameKey"], method = [RequestMethod.GET, RequestMethod.POST])
    fun renameKey(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<ValueVo> {
        return success(redisService.renameKey(commonRedisVo))
    }

    @RequestMapping(value = ["/delField"], method = [RequestMethod.GET, RequestMethod.POST])
    fun delField(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<String> {
        return success(redisService.delField(commonRedisVo))
    }

    @RequestMapping(value = ["/addField"], method = [RequestMethod.GET, RequestMethod.POST])
    fun addField(@RequestBody commonRedisVo: CommonRedisVo): WebApiRspDto<String> {
        return success(redisService.addField(commonRedisVo))
    }

    @RequestMapping(value = ["/actuator/health"], method = [RequestMethod.GET])
    fun health(): String {
        return SpringUtils.context.environment.activeProfiles.contentToString()
    }
}
