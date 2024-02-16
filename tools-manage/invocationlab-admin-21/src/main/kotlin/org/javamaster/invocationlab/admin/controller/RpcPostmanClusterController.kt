package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.enums.RegisterCenterType
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto.Companion.error
import org.javamaster.invocationlab.admin.service.AppFactory
import org.javamaster.invocationlab.admin.service.AppFactory.Companion.getRegisterFactory
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.google.common.collect.Maps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.util.stream.Collectors

/**
 * 目前提供动态添加和删除cluster的地址
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
class RpcPostmanClusterController {
    @Autowired
    private lateinit var redisRepository: RedisRepository

    @Autowired
    private lateinit var appFactory: AppFactory

    @Value("\${dubbo.postman.env}")
    private lateinit var env: String

    @RequestMapping(value = ["configs"], method = [RequestMethod.GET])
    @ResponseBody
    fun configs(): WebApiRspDto<List<Map<String, Any>>> {
        val sets = redisRepository.members<Any>(RedisKeys.CLUSTER_REDIS_KEY)
        return WebApiRspDto.success(setToMap(sets!!.stream().map { obj: Any -> obj.toString() }
            .collect(Collectors.toSet())))
    }

    @RequestMapping(value = ["all-zk"], method = [RequestMethod.GET])
    @ResponseBody
    fun allZk(): WebApiRspDto<List<Map<String, Any>>> {
        val zkAddrs = mutableSetOf<String>()
        SpringUtils.context.getBeansOfType(RegisterFactory::class.java).values.stream()
            .map { obj: RegisterFactory -> obj.getClusterSet() }
            .forEach(zkAddrs::addAll)
        return WebApiRspDto.success(setToMap(zkAddrs))
    }

    private fun setToMap(zkAddrs: Set<String>): List<Map<String, Any>> {
        return zkAddrs.stream()
            .map<Map<String, Any>> { addr: String ->
                var type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, addr)
                if (type == null) {
                    type = RegisterCenterType.ZK.type
                }
                val map: MutableMap<String, Any> = Maps.newHashMap()
                map["addr"] = addr
                map["type"] = type
                map
            }
            .collect(Collectors.toList())
    }

    @RequestMapping(value = ["new/config"], method = [RequestMethod.GET])
    @ResponseBody
    fun queryDubbo(
        @RequestParam(name = "zk") zk: String,
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "type") type: Int
    ): WebApiRspDto<String> {
        if (password != PASSWORD) {
            return error("密码错误")
        }
        if (zk.isEmpty()) {
            return error("注册中心地址不能为空")
        }
        val registerFactory = getRegisterFactory(type)
        if (registerFactory.getClusterSet().contains(zk)) {
            return error("注册中心地址已经存在")
        }
        registerFactory.addCluster(zk)
        registerFactory.get(zk)
        redisRepository.setAdd(RedisKeys.CLUSTER_REDIS_KEY, zk)
        redisRepository.mapPut(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk, type)
        return WebApiRspDto.success("保存成功")
    }

    @RequestMapping(value = ["zk/del"], method = [RequestMethod.GET])
    @ResponseBody
    fun del(
        @RequestParam(name = "zk") zk: String,
        @RequestParam(name = "password") password: String
    ): WebApiRspDto<String> {
        if (password != PASSWORD) {
            return error("密码错误")
        }
        if (zk.isEmpty()) {
            return error("zk不能为空")
        }
        val registerFactory = appFactory.getRegisterFactory(zk)
        if (!registerFactory.getClusterSet().contains(zk)) {
            return error("zk地址不存在")
        }
        registerFactory.getClusterSet().remove(zk)
        registerFactory.remove(zk)
        redisRepository.setRemove(RedisKeys.CLUSTER_REDIS_KEY, zk)
        redisRepository.removeMap(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk)
        return WebApiRspDto.success("删除成功")
    }

    @RequestMapping(value = ["env"], method = [RequestMethod.GET])
    @ResponseBody
    fun env(): WebApiRspDto<String> {
        return WebApiRspDto.success(env)
    }

    companion object {
        /**
         * 可执行修改其他的值,设置这个主要是防止随意修改注册地址
         */
        private const val PASSWORD = "123456"
    }
}
