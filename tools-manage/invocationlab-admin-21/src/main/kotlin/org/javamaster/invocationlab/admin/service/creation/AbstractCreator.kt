@file:Suppress("VulnerableCodeUsages")

package org.javamaster.invocationlab.admin.service.creation

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.context.InvokeContext.putService
import org.javamaster.invocationlab.admin.service.context.InvokeContext.removeService
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader
import org.javamaster.invocationlab.admin.service.maven.Maven
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.JsonUtils.objectToString
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.set
import com.alibaba.fastjson.JSONObject
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate
import java.io.File

/**
 * @author yudong
 * @date 2022/11/13
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractCreator : Creator {
    @JvmField
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    protected lateinit var redisRepository: RedisRepository

    @Autowired
    protected lateinit var maven: Maven

    @Autowired
    protected lateinit var restTemplate: RestTemplate

    @Value("\${nexus.url.search}")
    private lateinit var nexusPath: String

    protected fun resolveMavenDependencies(serviceName: String, gav: GAV) {
        logger.info("开始创建服务...")
        logger.info("如果系统是第一次构建服务则需要下载各种maven plugin,耗时比较长")
        maven.dependency(serviceName, gav)
    }

    protected fun saveToRedisAndInitLoader(postmanService: PostmanService) {
        val serviceString = objectToString(postmanService)
        val serviceKey = buildServiceKey(postmanService.getCluster(), postmanService.getServiceName())

        redisRepository.removeMap(RedisKeys.RPC_MODEL_KEY, serviceKey)
        redisRepository.mapPut(RedisKeys.RPC_MODEL_KEY, serviceKey, serviceString)
        redisRepository.setAdd(postmanService.getCluster(), postmanService.getServiceName())

        JarLocalFileLoader.loadInterfaceInfoAndInitLoader(postmanService)
        putService(serviceKey, postmanService)
    }


    override fun remove(cluster: String, serviceName: String) {
        val serviceKey = buildServiceKey(cluster, serviceName)

        val serviceObj = redisRepository.mapGet<String>(RedisKeys.RPC_MODEL_KEY, serviceKey)
        val postmanService: PostmanService = parseObject(serviceObj, DubboPostmanService::class.java)

        val jarLibPath = JarLocalFileLoader.getJarLibPath(serviceName, postmanService.getGav().version!!)
        val parentFile = File(jarLibPath).parentFile
        FileUtils.deleteDirectory(parentFile)
        logger.info("完成删除版本服务目录:{}", parentFile)

        redisRepository.removeMap(RedisKeys.RPC_MODEL_KEY, serviceKey)
        redisRepository.setRemove(cluster, serviceKey)
        removeService(serviceKey)
        logger.info("完成删除redis服务相关信息:{}", serviceKey)
    }


    protected fun upgradeGavVersionToLatest(gav: GAV) {
        val version = gav.version

        val jsonObject = restTemplate.getForObject(
            "$nexusPath?g={g}&a={a}&collapseresults=true",
            JSONObject::class.java, gav.groupID, gav.artifactID
        )
        val jsonArray = jsonObject!!.getJSONArray("data")
        val gavInfo = jsonArray[0] as Map<String, Any>
        val latestVersion = if (version!!.contains("SNAPSHOT")) {
            gavInfo["latestSnapshot"] as String
        } else {
            gavInfo["latestRelease"] as String
        }
        gav.version = latestVersion

        if (version != latestVersion) {
            set(ThreadLocalUtils.OLD_GAV_VERSION, version)
        }
    }

    override fun getLatestVersion(gav: GAV): String {
        val jsonObject = restTemplate.getForObject(
            "$nexusPath?g={g}&a={a}&collapseresults=true",
            JSONObject::class.java, gav.groupID, gav.artifactID
        )
        val jsonArray = jsonObject!!.getJSONArray("data")
        if (jsonArray.isEmpty()) {
            throw BizException(gav.toString() + "依赖不存在!!!")
        }
        val gavInfo = jsonArray[0] as Map<String, Any>
        var latestVersion: String
        if (StringUtils.isBlank(gav.version)) {
            latestVersion = gavInfo["latestSnapshot"] as String
            if (StringUtils.isBlank(latestVersion)) {
                latestVersion = gavInfo["latestRelease"] as String
            }
            return latestVersion
        }
        latestVersion = if (gav.version!!.contains("SNAPSHOT")) {
            gavInfo["latestSnapshot"] as String
        } else {
            gavInfo["latestRelease"] as String
        }
        return latestVersion
    }
}
