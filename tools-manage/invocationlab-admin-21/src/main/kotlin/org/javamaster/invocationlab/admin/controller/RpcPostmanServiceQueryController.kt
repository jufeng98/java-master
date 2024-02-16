package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.enums.RegisterCenterType
import org.javamaster.invocationlab.admin.enums.RegisterCenterType.Companion.getByType
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto
import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto.Companion.error
import org.javamaster.invocationlab.admin.service.AppFactory.Companion.getRegisterFactory
import org.javamaster.invocationlab.admin.service.context.InvokeContext.getService
import org.javamaster.invocationlab.admin.service.context.InvokeContext.tryLoadRuntimeInfo
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader.allClassLoader
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.ClassUtils.getParamGenericType
import org.javamaster.invocationlab.admin.util.ClassUtils.shouldNew
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Parameter
import java.util.*
import java.util.stream.Collectors

/**
 * 应用详细信息查询相关
 *
 * @author yudong
 */
@Suppress("VulnerableCodeUsages")
@Controller
@RequestMapping("/dubbo-postman/")
class RpcPostmanServiceQueryController : AbstractController() {
    @Autowired
    private lateinit var redisRepository: RedisRepository

    /**
     * 返回已经注册的服务名称
     *
     * @param zk 指定的zk地址
     * @return 返回指定zk下面的所有已经注册的服务名称
     */
    @RequestMapping(value = ["result/serviceNames"], method = [RequestMethod.GET])
    @ResponseBody
    fun serviceNames(@RequestParam(value = "zk") zk: String): WebApiRspDto<Set<Any>> {
        val serviceNameSet = redisRepository.members<Any>(zk)
        return WebApiRspDto.success(
            serviceNameSet!!.stream().sorted().collect(
                Collectors.toCollection { LinkedHashSet() }
            )
        )
    }

    @RequestMapping(value = ["result/serviceNamesJdk"], method = [RequestMethod.GET])
    @ResponseBody
    fun serviceNamesJdk(): WebApiRspDto<Set<Any>> {
        val serviceNameSet = redisRepository.members<Any>(RedisKeys.RPC_MODEL_JDK_KEY)
        return WebApiRspDto.success(
            serviceNameSet!!.stream().sorted().collect(
                Collectors.toCollection { LinkedHashSet() }
            )
        )
    }

    /**
     * 返回所有的接口
     *
     * @param interfaceKey 接口key group/interfaceName:version
     */
    @RequestMapping(value = ["result/interface"], method = [RequestMethod.GET])
    @ResponseBody
    fun getInterfaces(
        @RequestParam("zk") zk: String,
        @RequestParam("serviceName") serviceName: String,
        @RequestParam("interfaceKey") interfaceKey: String
    ): WebApiRspDto<InterfaceEntity> {
        val serviceKey = buildServiceKey(zk, serviceName)
        val service = getService(serviceKey) ?: return error("服务不存在,请先创建或刷新服务!")

        val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk)
        val registrationCenterType = getByType(type)
        tryLoadRuntimeInfo(service, registrationCenterType!!, serviceKey)

        val registerFactory = getRegisterFactory(type!!)
        val register = registerFactory.get(zk)
        val serviceMap: Map<String, InterfaceMetaInfo>
        var res: InterfaceEntity? = null
        if (registrationCenterType == RegisterCenterType.ZK) {
            serviceMap = register.getAllService()[serviceName]!!
            for (interfaceModel in service.getInterfaceModels()) {
                val metaItem = serviceMap[interfaceKey]
                //根据接口名称匹配接口对应的服务
                if (interfaceModel.key == interfaceKey) {
                    //用于实时同步应用的所有ip
                    interfaceModel.serverIps = metaItem!!.serverIps
                    res = interfaceModel
                    break
                }
            }
        } else if (registrationCenterType == RegisterCenterType.EUREKA) {
            for (interfaceModel in service.getInterfaceModels()) {
                if (interfaceModel.key == interfaceKey) {
                    interfaceModel.serverIps = HashSet(register.getServiceInstances(serviceName))
                    res = interfaceModel
                }
            }
        } else {
            throw RuntimeException("error:$type")
        }
        val names = res!!.methodNames.stream()
            .sorted()
            .collect(
                Collectors.toCollection { LinkedHashSet() }
            )
        res.methodNames = names
        val methodEntities = res.methods.stream()
            .sorted(Comparator.comparing { it.name!! })
            .collect(Collectors.toList())
        res.methods = methodEntities
        return WebApiRspDto.success(res)
    }

    /**
     * 根据服务名称,接口key,方法路径,获取参数模板
     */
    @RequestMapping(value = ["result/interface/method/param"], method = [RequestMethod.GET])
    @ResponseBody
    fun getResultServiceMethod(
        @RequestParam("zk") zk: String,
        @RequestParam("serviceName") serviceName: String,
        @RequestParam("interfaceKey") interfaceKey: String,
        @RequestParam("methodPath") methodPath: String
    ): WebApiRspDto<Map<String, Any>> {
        val serviceKey = buildServiceKey(zk, serviceName)
        val service = getService(serviceKey) ?: return error("服务不存在,请先创建或刷新服务!")

        val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk)
        val registrationCenterType = getByType(type)
        tryLoadRuntimeInfo(service, registrationCenterType!!, serviceKey)

        val param: MutableMap<String, Any> = Maps.newLinkedHashMap()
        //重启之后在访问的时候重新load
        val classLoader = allClassLoader[serviceKey] ?: return error("加载类异常,classLoader为null")
        for (serviceModel in service.getInterfaceModels()) {
            if (serviceModel.key != interfaceKey) {
                continue
            }
            for (methodModel in serviceModel.methods) {
                if (methodModel.name != methodPath) {
                    continue
                }
                for (parameter in methodModel.method!!.parameters) {
                    val value = newObj(parameter, classLoader)
                    param[parameter.name] = JSONObject.toJSON(value)
                }
            }
        }
        return WebApiRspDto.success(param)
    }

    /**
     * 返回所有的接口
     *
     * @param serviceName 服务名称
     */
    @RequestMapping(value = ["result/interfaceNames"], method = [RequestMethod.GET])
    @ResponseBody
    fun getInterfaces(
        @RequestParam("zk") zk: String,
        @RequestParam("serviceName") serviceName: String
    ): WebApiRspDto<Map<String, String>> {
        val interfaceMap = getAllSimpleClassName(zk, serviceName)
        return WebApiRspDto.success(interfaceMap)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RpcPostmanServiceQueryController::class.java)

        private fun newObj(parameter: Parameter, classLoader: ClassLoader): Any? {
            val clazz = parameter.type
            if (MutableCollection::class.java.isAssignableFrom(clazz)) {
                val genericType = getParamGenericType(parameter.parameterizedType)
                val obj: Any?
                if (shouldNew(genericType)) {
                    val aClass = Class.forName(genericType!!.typeName, true, classLoader)
                    obj = newObj(aClass, classLoader, 1)
                } else {
                    obj = null
                }
                return if (MutableList::class.java.isAssignableFrom(clazz)) Lists.newArrayList(obj) else Sets.newHashSet(
                    obj
                )
            } else {
                return if (shouldNew(clazz)) {
                    newObj(clazz, classLoader, 1)
                } else {
                    null
                }
            }
        }

        private fun createClassInstance(clazz: Class<*>): Any {
            val constructors = clazz.declaredConstructors
            AccessibleObject.setAccessible(constructors, true)

            val map = Arrays.stream(constructors)
                .collect(
                    Collectors.groupingBy { it: Constructor<*> -> it.parameterCount == 0 }
                )

            val defConstructors = map[true]
            if (defConstructors != null) {
                return defConstructors[0].newInstance()
            }

            val otherConstructors = map[false]!!.stream()
                .sorted(Comparator.comparingInt { obj: Constructor<*> -> obj.parameterCount })
                .collect(Collectors.toList())

            val constructor = otherConstructors[0]
            val parameterCount = constructor.parameterCount
            val objects = arrayOfNulls<Any>(parameterCount)
            return constructor.newInstance(*objects)
        }

        private fun newObj(clazz: Class<*>, classLoader: ClassLoader, deep: Int): Any? {
            var newDeep = deep
            if (clazz.isEnum) {
                return null
            }
            val `object`: Any
            try {
                `object` = createClassInstance(clazz)
            } catch (e: Exception) {
                logger.error(e.message)
                return null
            }
            if (newDeep >= 3) {
                return `object`
            }
            for (declaredField in clazz.declaredFields) {
                val fieldType = declaredField.type
                declaredField.isAccessible = true
                if (MutableCollection::class.java.isAssignableFrom(fieldType)) {
                    val isList = MutableList::class.java.isAssignableFrom(fieldType)
                    val genericType = getParamGenericType(declaredField.genericType)
                    if (shouldNew(genericType)) {
                        try {
                            val fieldObj = newObj(
                                Class.forName(
                                    genericType!!.typeName, true, classLoader
                                ),
                                classLoader, ++newDeep
                            )
                            declaredField[`object`] =
                                if (isList) Lists.newArrayList(fieldObj) else Sets.newHashSet(fieldObj)
                        } catch (e: Exception) {
                            logger.error(e.message)
                            declaredField[`object`] = if (isList) Lists.newArrayList() else Sets.newHashSet<Any>()
                        }
                    } else {
                        declaredField[`object`] = if (isList) Lists.newArrayList() else Sets.newHashSet<Any>()
                    }
                } else {
                    if (shouldNew(fieldType)) {
                        try {
                            val fieldObj = newObj(fieldType, classLoader, ++newDeep)
                            declaredField[`object`] = fieldObj
                        } catch (e: Exception) {
                            logger.error(e.message)
                        }
                    }
                }
            }
            return `object`
        }
    }
}
