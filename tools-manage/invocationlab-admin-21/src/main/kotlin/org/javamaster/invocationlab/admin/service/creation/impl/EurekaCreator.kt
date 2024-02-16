package org.javamaster.invocationlab.admin.service.creation.impl

import org.javamaster.invocationlab.admin.consts.Constant
import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.context.InvokeContext.putFeignContext
import org.javamaster.invocationlab.admin.service.creation.AbstractCreator
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader
import org.javamaster.invocationlab.admin.service.registry.impl.EurekaRegister
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.util.BuildUtils.buildInterfaceKey
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import com.google.common.collect.Lists
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.FeignServiceRegistrar.register
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.util.*
import java.util.jar.JarFile
import java.util.stream.Collectors

/**
 * @author yudong
 */
@Component
class EurekaCreator : AbstractCreator() {
    override fun create(cluster: String, gav: GAV, serviceName: String): Pair<Boolean, String> {
        resolveMavenDependencies(serviceName, gav)

        val postmanService = DubboPostmanService()
        postmanService.cluster = cluster
        postmanService.serviceName = serviceName
        postmanService.gav = gav
        postmanService.generateTime = System.currentTimeMillis()

        JarLocalFileLoader.initClassLoader(serviceName, gav.version!!).use {
            val feignClientClasses = findAllFeignClientClassFromLibJars(serviceName, gav, it)
            val feignServicePackages = findFeignServicePackages(feignClientClasses)
            val list = getAllFeignClientMethods(feignClientClasses)

            postmanService.getInterfaceModels().addAll(list)

            saveToRedisAndInitLoader(postmanService)

            val serviceKey = buildServiceKey(postmanService.getCluster(), postmanService.getServiceName())
            val apiJarClassLoader = JarLocalFileLoader.allClassLoader[serviceKey]
            val context = register(feignServicePackages, apiJarClassLoader!!)
            putFeignContext(serviceKey, context)

            return Pair(true, "成功")
        }
    }

    private fun findFeignServicePackages(feignClientClasses: List<Class<*>>): List<String> {
        return feignClientClasses.stream()
            .map { it: Class<*> -> it.getPackage().name }
            .distinct()
            .collect(Collectors.toList())
    }

    override fun refresh(cluster: String, serviceName: String): Pair<Boolean, String> {
        EurekaRegister.clearCache()
        val serviceKey = buildServiceKey(cluster, serviceName)
        val serviceObj = redisRepository.mapGet<Any>(RedisKeys.RPC_MODEL_KEY, serviceKey)

        val postmanService: PostmanService = parseObject(serviceObj as String, DubboPostmanService::class.java)
        val gav = postmanService.getGav()

        val oldInfo = gav.toString()
        upgradeGavVersionToLatest(gav)
        val newInfo = gav.toString()

        val pair = create(cluster, gav, serviceName)

        return Pair(pair.left, "刷新服务 $oldInfo => $newInfo 成功!")
    }

    fun initFeignInfoAndPutContext(service: PostmanService, serviceKey: String) {
        val apiJarClassLoader = JarLocalFileLoader.allClassLoader[serviceKey]

        val feignClientClasses = findAllFeignClientClassFromLibJars(
            service.getServiceName(), service.getGav(),
            apiJarClassLoader!!
        )
        val feignServicePackages = findFeignServicePackages(feignClientClasses)

        val context = register(feignServicePackages, apiJarClassLoader)
        putFeignContext(serviceKey, context)
    }


    private fun findAllFeignClientClassFromLibJars(
        serviceName: String,
        gav: GAV,
        apiJarClassLoader: ApiJarClassLoader
    ): List<Class<*>> {
        val apiFilePath = JarLocalFileLoader.getApiFilePath(serviceName, gav)

        JarFile(apiFilePath).use {
            val enumeration = it.entries()

            val list: MutableList<Class<*>> = Lists.newArrayList()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                var name = jarEntry.name
                if (!name.endsWith(".class")) {
                    continue
                }
                name = name.replace(".class", "").replace("/", ".")
                var aClass: Class<*>
                try {
                    aClass = apiJarClassLoader.loadClassWithResolve(name)
                } catch (e: Throwable) {
                    logger.error("load error:{}", name, e)
                    continue
                }
                if (!aClass.isInterface) {
                    continue
                }
                aClass.getAnnotation(FeignClient::class.java) ?: continue
                list.add(aClass)
            }
            return list
        }
    }

    fun getAllFeignClientMethods(classes: List<Class<*>>): MutableList<InterfaceEntity> {
        return classes.stream()
            .map { aClass: Class<*> ->
                val interfaceModel = InterfaceEntity()
                val providerName = aClass.name
                val methodNames: Set<String>
                try {
                    methodNames = Arrays.stream(aClass.declaredMethods)
                        .map { obj: Method -> obj.name }
                        .collect(Collectors.toSet())
                } catch (e: Throwable) {
                    logger.error("get method error:{}", aClass, e)
                    return@map null
                }
                interfaceModel.interfaceName = providerName
                interfaceModel.methodNames = methodNames
                interfaceModel.serverIps = emptySet()
                interfaceModel.version = Constant.DEFAULT_VERSION
                interfaceModel.group = Constant.GROUP_DEFAULT
                interfaceModel.key = buildInterfaceKey(interfaceModel.group!!, providerName, interfaceModel.version)
                interfaceModel
            }
            .filter { obj -> Objects.nonNull(obj) }
            .collect(Collectors.toList())
    }
}
