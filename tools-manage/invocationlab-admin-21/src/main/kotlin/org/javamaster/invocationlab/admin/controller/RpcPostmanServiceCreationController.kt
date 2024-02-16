package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto.Companion.error
import org.javamaster.invocationlab.admin.service.AppFactory
import org.javamaster.invocationlab.admin.service.AppFactory.Companion.getRegisterFactory
import org.javamaster.invocationlab.admin.service.context.InvokeContext.getService
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.javamaster.invocationlab.admin.util.XmlUtils.parseGav
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.util.stream.Collectors

/**
 * 提供服务的创建及刷新的功能
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
class RpcPostmanServiceCreationController {
    @Autowired
    private lateinit var redisRepository: RedisRepository

    @Autowired
    private lateinit var appFactory: AppFactory

    /**
     * 根据zk,返回所有的应用名称,这个是zk里面的,还未创建
     */
    @RequestMapping(value = ["result/appNames"], method = [RequestMethod.GET])
    @ResponseBody
    fun getAppNames(@RequestParam("zk") zk: String): WebApiRspDto<Set<String>> {
        if (zk.isEmpty()) {
            return error("zk地址必须指定")
        }
        val registerFactory = appFactory.getRegisterFactory(zk)
        val services: Map<String, Map<String, InterfaceMetaInfo>> = registerFactory.get(zk).getAllService()
        return WebApiRspDto.success(
            services.keys.stream()
                .sorted()
                .collect(Collectors.toCollection { LinkedHashSet() })
        )
    }

    @RequestMapping(value = ["create"], method = [RequestMethod.GET])
    @ResponseBody
    fun createService(
        @RequestParam("zk") zk: String,
        @RequestParam("zkServiceName") serviceName: String,
        @RequestParam("dependency") dependency: String
    ): WebApiRspDto<String> {
        if (serviceName.isEmpty()) {
            return error("必须选择一个服务名称", -1)
        }
        if (dependency.isEmpty()) {
            return error("dependency不能为空")
        }

        val gav = parseGav(dependency)
        if (StringUtils.isBlank(gav.version)) {
            val creator = appFactory.getCreator(
                zk
            )
            val latestVersion = creator.getLatestVersion(gav)
            gav.version = latestVersion
        }

        val creator = appFactory.getCreator(
            zk
        )
        val pair = creator.create(
            zk, gav, serviceName
        )
        if (!pair.left) {
            return error(pair.right)
        }
        return WebApiRspDto.success(pair.right)
    }

    @RequestMapping(value = ["createJdk"], method = [RequestMethod.GET])
    @ResponseBody
    fun createJdk(@RequestParam("dependency") dependency: String): WebApiRspDto<String> {
        if (dependency.isEmpty()) {
            return error("dependency不能为空")
        }

        val gav = parseGav(dependency)

        val creator: JdkCreator = SpringUtils.context.getBean(JdkCreator::class.java)
        val latestVersion = creator.getLatestVersion(gav)
        gav.version = latestVersion

        val pair = creator.create("", gav, gav.artifactID!!)
        if (!pair.left) {
            return error(pair.right)
        }
        return WebApiRspDto.success(pair.right)
    }

    @RequestMapping(value = ["refresh"], method = [RequestMethod.GET])
    @ResponseBody
    fun refreshService(
        @RequestParam("zk") zk: String,
        @RequestParam("zkServiceName") serviceName: String,
        @RequestParam("dubbo") dubbo: Boolean
    ): WebApiRspDto<String> {
        if (serviceName.isEmpty()) {
            return error("必须选择一个服务名称", -1)
        }
        if (dubbo) {
            val modelKey = buildServiceKey(zk, serviceName)
            val postmanService = getService(modelKey)
            val names: List<String> = postmanService!!.getInterfaceModels().stream()
                .map { it.interfaceName!! }
                .collect(Collectors.toList())
            val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk)
            val registerFactory = getRegisterFactory(type!!)
            registerFactory.refreshService(names, zk)
        }

        val creator = appFactory.getCreator(
            zk
        )
        val pair = creator.refresh(
            zk, serviceName
        )

        if (!pair.left) {
            return error(pair.right)
        }
        return WebApiRspDto.success(pair.right)
    }

    @RequestMapping(value = ["refreshJdk"], method = [RequestMethod.GET])
    @ResponseBody
    fun refreshServiceJdk(@RequestParam("dependencyName") serviceName: String): WebApiRspDto<String> {
        if (serviceName.isEmpty()) {
            return error("必须选择一个依赖名称", -1)
        }
        val creator: JdkCreator = SpringUtils.context.getBean(JdkCreator::class.java)
        val pair = creator.refresh("", serviceName)
        if (!pair.left) {
            return error(pair.right)
        }
        return WebApiRspDto.success(pair.right)
    }

    @RequestMapping(value = ["delService"], method = [RequestMethod.GET])
    @ResponseBody
    fun delService(@RequestParam("zkServiceName") serviceName: String): WebApiRspDto<String> {
        val zkAddrs = mutableSetOf<String>()
        SpringUtils.context.getBeansOfType(RegisterFactory::class.java).values.stream()
            .map { obj: RegisterFactory -> obj.getClusterSet() }
            .forEach(zkAddrs::addAll)
        var del = false
        for (addr in zkAddrs) {
            val serviceNameSet = redisRepository.members<Any>(addr)
            if (!serviceNameSet!!.contains(serviceName)) {
                continue
            }
            del = true
            redisRepository.setRemove(addr, serviceName)
            val creator = appFactory.getCreator(
                addr
            )
            creator.remove(addr, serviceName)
        }
        if (!del) {
            return error("服务名有误")
        }
        return WebApiRspDto.success("删除服务成功")
    }
}
