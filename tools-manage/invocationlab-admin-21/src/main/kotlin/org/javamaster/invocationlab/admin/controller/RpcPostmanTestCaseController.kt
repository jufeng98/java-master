package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.dto.UserCaseDto
import org.javamaster.invocationlab.admin.model.dto.UserCaseGroupDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.util.CollectionUtils
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

/**
 * 用例相关的操作
 * 用例在这个系统里面指一个接口的请求,目前来说是对一个dubbo接口的请求[UserCaseDto]
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
class RpcPostmanTestCaseController : AbstractController() {
    @Autowired
    private lateinit var redisRepository: RedisRepository

    @RequestMapping(value = ["case/save"], method = [RequestMethod.POST])
    @ResponseBody
    fun saveCase(@RequestBody caseDto: UserCaseDto): WebApiRspDto<Boolean> {
        val groupName = caseDto.groupName
        redisRepository.setAdd(RedisKeys.CASE_KEY, groupName!!)

        val value = JsonUtils.jacksonObjectMapper.writeValueAsString(caseDto)
        redisRepository.mapPut(RedisKeys.CASE_KEY_SUB + "_" + groupName, caseDto.caseName!!, value)

        return WebApiRspDto.success(java.lang.Boolean.TRUE)
    }

    @ResponseBody
    @RequestMapping(value = ["case/group-case-detail/list"], method = [RequestMethod.GET])
    fun allGroupCaseDetail(): WebApiRspDto<List<UserCaseDto>> {
        val groupNames = redisRepository.members<String>(RedisKeys.CASE_KEY)
        val groupDtoList = groupNames!!.stream()
            .map { groupName: String ->
                val caseNames = redisRepository.mapGetKeys<String>(RedisKeys.CASE_KEY_SUB + "_" + groupName)
                caseNames.stream()
                    .map { caseName: String ->
                        val jsonStr = redisRepository.mapGet<String>(RedisKeys.CASE_KEY_SUB + "_" + groupName, caseName)
                        JsonUtils.jacksonObjectMapper.readValue(
                            jsonStr,
                            Any::class.java
                        ) as UserCaseDto
                    }
                    .collect(Collectors.toList())
            }
            .flatMap { obj: List<UserCaseDto> -> obj.stream() }
            .collect(Collectors.toList())
        return WebApiRspDto.success(groupDtoList)
    }

    @ResponseBody
    @RequestMapping(value = ["case/group/list"], method = [RequestMethod.GET])
    fun allGroupAndCaseName(): WebApiRspDto<List<UserCaseGroupDto>> {
        val groupNames = redisRepository.members<String>(RedisKeys.CASE_KEY)

        val groupDtoList = groupNames!!.stream()
            .map { groupName: String ->
                val parentDto = UserCaseGroupDto()
                parentDto.value = groupName
                parentDto.label = groupName

                val caseNames = redisRepository.mapGetKeys<String>(RedisKeys.CASE_KEY_SUB + "_" + groupName)

                val children = caseNames.stream()
                    .map { caseName: String ->
                        val dto = UserCaseGroupDto()
                        dto.value = caseName
                        dto.label = caseName
                        dto.children = emptyList()
                        dto
                    }
                    .collect(Collectors.toList())

                parentDto.children = children
                parentDto
            }
            .sorted { g1: UserCaseGroupDto, g2: UserCaseGroupDto ->
                g1.label!!.compareTo(
                    g2.label!!, ignoreCase = true
                )
            }
            .collect(Collectors.toList())
        return WebApiRspDto.success(groupDtoList)
    }

    @ResponseBody
    @RequestMapping(value = ["case/group-name/list"], method = [RequestMethod.GET])
    fun allGroupName(): WebApiRspDto<MutableList<String?>> {
        val groupNames = redisRepository.members<Any>(RedisKeys.CASE_KEY)

        val groupDtoList = groupNames!!.stream()
            .map { obj: Any ->
                val groupDto = UserCaseGroupDto()
                groupDto.value = obj.toString()
                groupDto.label = obj.toString()
                groupDto.children = emptyList()
                groupDto.value
            }
            .collect(Collectors.toList())
        return WebApiRspDto.success(groupDtoList)
    }

    @RequestMapping(value = ["case/detail"], method = [RequestMethod.GET])
    @ResponseBody
    fun queryCaseDetail(
        @RequestParam(value = "groupName") groupName: String,
        @RequestParam(value = "caseName") caseName: String
    ): WebApiRspDto<UserCaseDto> {
        val jsonStr = redisRepository.mapGet<String>(RedisKeys.CASE_KEY_SUB + "_" + groupName, caseName)
        val caseDto = JsonUtils.jacksonObjectMapper.readValue(jsonStr, Any::class.java) as UserCaseDto

        if (caseDto.className == null) {
            val classNameMap = getAllSimpleClassName(caseDto.zkAddress!!, caseDto.serviceName!!)
            for ((key, value) in classNameMap) {
                if (value == caseDto.interfaceKey) {
                    caseDto.className = key
                    break
                }
            }
        }

        return WebApiRspDto.success(caseDto)
    }

    @RequestMapping(value = ["case/delete"], method = [RequestMethod.GET])
    @ResponseBody
    fun deleteDetail(
        @RequestParam(value = "groupName") groupName: String,
        @RequestParam(value = "caseName") caseName: String
    ): WebApiRspDto<String> {
        redisRepository.removeMap(groupName, caseName)
        val caseNames: Set<String> = redisRepository.mapGetKeys(groupName)

        if (CollectionUtils.isEmpty(caseNames)) {
            redisRepository.delete(RedisKeys.CASE_KEY_SUB + "_" + groupName)
            redisRepository.setRemove(RedisKeys.CASE_KEY, groupName)
        }

        return WebApiRspDto.success("删除成功")
    }
}
