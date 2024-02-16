package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.dto.SceneCaseDto
import org.javamaster.invocationlab.admin.model.dto.UserCaseDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.JsonUtils.objectToString
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

/**
 * 场景相关的操作
 * 一个场景用例包含多个用例的请求响应,多个请求依赖前面的响应结果[SceneCaseDto]
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
class RpcPostmanSceneTestController {
    @Autowired
    private lateinit var cacheService: RedisRepository

    @RequestMapping(value = ["case/scene/save"], method = [RequestMethod.POST])
    @ResponseBody
    fun saveSceneCase(@RequestBody caseDto: SceneCaseDto): WebApiRspDto<Boolean> {
        val caseName = caseDto.caseName
        val value = objectToString(caseDto)
        cacheService.mapPut(RedisKeys.SCENE_CASE_KEY, caseName!!, value)
        return WebApiRspDto.success(java.lang.Boolean.TRUE)
    }

    @RequestMapping(value = ["case/scene/delete"], method = [RequestMethod.GET])
    @ResponseBody
    fun deleteSceneCase(@RequestParam caseName: String): WebApiRspDto<Boolean> {
        cacheService.removeMap(RedisKeys.SCENE_CASE_KEY, caseName)
        return WebApiRspDto.success(java.lang.Boolean.TRUE)
    }

    @RequestMapping(value = ["case/scene/get"], method = [RequestMethod.GET])
    @ResponseBody
    fun getSceneCase(@RequestParam("caseName") caseName: String): WebApiRspDto<SceneCaseDto> {
        val testCaseDtoList: MutableList<UserCaseDto> = ArrayList()
        val value = cacheService.mapGet<String>(RedisKeys.SCENE_CASE_KEY, caseName)
        val sceneCaseDto = parseObject(value, SceneCaseDto::class.java)
        val identifyCaseDtos = sceneCaseDto.caseDtoList
        for (identifyCaseDto in identifyCaseDtos) {
            val jsonStr = cacheService.mapGet<String>(identifyCaseDto.groupName!!, identifyCaseDto.caseName!!)
            val caseDto = parseObject(jsonStr, UserCaseDto::class.java)
            testCaseDtoList.add(caseDto)
        }
        val rspSceneCaseDto = SceneCaseDto()
        rspSceneCaseDto.caseDtoList = testCaseDtoList
        rspSceneCaseDto.sceneScript = sceneCaseDto.sceneScript
        return WebApiRspDto.success(rspSceneCaseDto)
    }

    @ResponseBody
    @RequestMapping(value = ["case/scene-name/list"], method = [RequestMethod.GET])
    fun allSceneName(): WebApiRspDto<Set<Any>> {
        val groupNames = cacheService.mapGetKeys<Any>(RedisKeys.SCENE_CASE_KEY)
        return WebApiRspDto.success(groupNames)
    }
}
