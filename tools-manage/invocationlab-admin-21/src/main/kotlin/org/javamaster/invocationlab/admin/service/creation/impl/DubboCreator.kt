package org.javamaster.invocationlab.admin.service.creation.impl

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.creation.AbstractCreator
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo
import org.javamaster.invocationlab.admin.service.registry.impl.DubboRegisterFactory
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.JsonUtils.parseObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author yudong
 */
@Component
class DubboCreator : AbstractCreator() {
    @Autowired
    private lateinit var dubboRegisterFactory: DubboRegisterFactory

    override fun create(cluster: String, gav: GAV, serviceName: String): Pair<Boolean, String> {
        val providers = dubboRegisterFactory.get(cluster).getAllService()[serviceName]
            ?: throw ErdException("服务名已不存在:$serviceName")
        val dubboPostmanService = DubboPostmanService()
        dubboPostmanService.cluster = cluster
        dubboPostmanService.serviceName = serviceName

        for (entry in providers.entries) {
            val dubboInterfaceModel = createInterfaceEntity(entry)
            dubboPostmanService.getInterfaceModels().add(dubboInterfaceModel)
        }
        dubboPostmanService.gav = gav
        dubboPostmanService.generateTime = System.currentTimeMillis()
        return doCreateService(serviceName, dubboPostmanService)
    }

    override fun refresh(cluster: String, serviceName: String): Pair<Boolean, String> {
        val serviceKey = buildServiceKey(cluster, serviceName)
        val serviceObj = redisRepository.mapGet<Any>(RedisKeys.RPC_MODEL_KEY, serviceKey)
        val postmanService: PostmanService = parseObject(serviceObj as String, DubboPostmanService::class.java)
        val gav = postmanService.getGav()

        val oldInfo = gav.groupID + ":" + gav.artifactID + ":" + gav.version
        upgradeGavVersionToLatest(gav)
        val newInfo = gav.groupID + ":" + gav.artifactID + ":" + gav.version

        val pair = create(cluster, gav, serviceName)
        return Pair(pair.left, "刷新服务 $oldInfo => $newInfo 成功!")
    }

    private fun doCreateService(
        serviceName: String,
        postmanService: PostmanService
    ): Pair<Boolean, String> {
        val gav = postmanService.getGav()
        resolveMavenDependencies(serviceName, gav)

        saveToRedisAndInitLoader(postmanService)

        return Pair(true, gav.toString())
    }

    companion object {
        private fun createInterfaceEntity(entry: Map.Entry<String, InterfaceMetaInfo>): InterfaceEntity {
            val dubboInterfaceModel = InterfaceEntity()
            val providerName = entry.value.interfaceName
            val version = entry.value.version
            val serverIps = entry.value.serverIps
            val methodNames = entry.value.methodNames
            dubboInterfaceModel.key = entry.key
            dubboInterfaceModel.interfaceName = providerName
            dubboInterfaceModel.methodNames = methodNames
            dubboInterfaceModel.serverIps = serverIps
            dubboInterfaceModel.version = version
            dubboInterfaceModel.group = entry.value.group
            return dubboInterfaceModel
        }
    }
}
