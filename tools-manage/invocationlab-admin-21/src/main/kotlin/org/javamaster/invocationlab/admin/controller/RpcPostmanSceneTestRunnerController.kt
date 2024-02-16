package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.dto.SceneCaseDto
import org.javamaster.invocationlab.admin.model.dto.UserCaseDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.service.scenetest.SceneTester
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 批量处理用例
 * 场景测试运行相关的操作
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
class RpcPostmanSceneTestRunnerController {
    @Autowired
    private lateinit var cacheService: RedisRepository

    @Autowired
    private lateinit var requestService: SceneTester

    @ResponseBody
    @RequestMapping(value = ["case/scene/run"], method = [RequestMethod.POST])
    fun runSceneCase(@RequestBody sceneDto: SceneCaseDto): WebApiRspDto<Map<String, Any>> {
        val testCaseDtoList: MutableList<UserCaseDto> = ArrayList(1)
        for (dto in sceneDto.caseDtoList) {
            val jsonStr = cacheService.mapGet<String>(dto.groupName!!, dto.caseName!!)
            val caseDto = parseObject(jsonStr, UserCaseDto::class.java)
            testCaseDtoList.add(caseDto)
        }
        val rst = requestService.process(testCaseDtoList, sceneDto.sceneScript!!)
        return WebApiRspDto.success(rst)
    }
}
