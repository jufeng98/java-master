package org.javamaster.invocationlab.admin.service.creation.impl

import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.creation.AbstractCreator
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.JsonUtils.objectToString
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.get
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.remove
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.net.URL
import java.util.*

/**
 * @author yudong
 */

@Component
class JdkCreator : AbstractCreator() {

    override fun create(cluster: String, gav: GAV, serviceName: String): Pair<Boolean, String> {
        val service = DubboPostmanService()
        service.serviceName = serviceName
        service.gav = gav
        service.generateTime = System.currentTimeMillis()

        return doCreateService(service)
    }


    override fun refresh(cluster: String, serviceName: String): Pair<Boolean, String> {
        globalJdkClassLoader.close()
        log.info("完成销毁redis旧加载器:{}", globalJdkClassLoader.javaClass)

        val serviceJson = redisRepository.mapGet<String>(RedisKeys.RPC_MODEL_JDK_MAP_KEY, serviceName)
        val service = parseObject(serviceJson, DubboPostmanService::class.java)

        upgradeGavVersionToLatest(service.getGav())

        val oldGavVersion = get<String>(ThreadLocalUtils.OLD_GAV_VERSION)
        if (StringUtils.isNotBlank(oldGavVersion)) {
            val jarLibPath = JarLocalFileLoader.getJarLibPath(serviceName, oldGavVersion!!)
            val parentFile = File(jarLibPath).parentFile
            FileUtils.deleteDirectory(parentFile)
            remove(ThreadLocalUtils.OLD_GAV_VERSION)
            logger.info("完成删除redis旧版本服务目录:{}", parentFile)
        }

        return doCreateService(service)
    }

    private fun doCreateService(service: PostmanService): Pair<Boolean, String> {
        val gav = service.getGav()

        val serviceName = service.getServiceName()
        redisRepository.setAdd(RedisKeys.RPC_MODEL_JDK_KEY, serviceName)

        resolveMavenDependencies(serviceName, gav)

        val serviceJson = objectToString(service)
        redisRepository.mapPut(RedisKeys.RPC_MODEL_JDK_MAP_KEY, serviceName, serviceJson)

        initGlobalJdkClassLoader()

        return Pair(true, "解析" + gav + "依赖成功!!!")
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(Companion::class.java)
        lateinit var globalJdkClassLoader: ApiJarClassLoader


        fun initGlobalJdkClassLoader() {
            val redisRepository: RedisRepository = SpringUtils.context.getBean(RedisRepository::class.java)
            val serviceNameSet = redisRepository.members<Any>(RedisKeys.RPC_MODEL_JDK_KEY)

            val urls = serviceNameSet!!.stream()
                .map { serviceName: Any ->
                    redisRepository.mapGet<Any>(
                        RedisKeys.RPC_MODEL_JDK_MAP_KEY,
                        serviceName
                    ) as String
                }
                .filter { obj: String -> Objects.nonNull(obj) }
                .map<List<URL>> { serviceJson: String ->
                    val service = parseObject(serviceJson, DubboPostmanService::class.java)
                    val jarLibPath =
                        JarLocalFileLoader.getJarLibPath(service.getServiceName(), service.getGav().version!!)
                    JarLocalFileLoader.getJarLibUrls(jarLibPath)
                }
                .flatMap { obj: List<URL> -> obj.stream() }
                .toArray { arrayOf<URL>() }

            globalJdkClassLoader = ApiJarClassLoader(urls)
            log.info("共引入{}个jar创建redis新加载器:{}", urls.size, globalJdkClassLoader.javaClass)
        }
    }
}
