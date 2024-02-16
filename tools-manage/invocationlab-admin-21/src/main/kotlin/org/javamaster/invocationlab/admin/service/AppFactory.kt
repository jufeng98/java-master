package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.enums.RegisterCenterType
import org.javamaster.invocationlab.admin.enums.RegisterCenterType.Companion.getByType
import org.javamaster.invocationlab.admin.service.creation.Creator
import org.javamaster.invocationlab.admin.service.creation.impl.DubboCreator
import org.javamaster.invocationlab.admin.service.creation.impl.EurekaCreator
import org.javamaster.invocationlab.admin.service.invocation.Invoker
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory
import org.javamaster.invocationlab.admin.service.registry.impl.DubboRegisterFactory
import org.javamaster.invocationlab.admin.service.registry.impl.EurekaRegisterFactory
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * @author yudong
 * @date 2022/11/12
 */
@Component
class AppFactory {
    @Autowired
    private lateinit var redisRepository: RedisRepository

    @Autowired
    private lateinit var dubboInvoker: Invoker<Any, PostmanDubboRequest>

    @Autowired
    @Qualifier("feignInvoker")
    private lateinit var feignInvoker: Invoker<Any, PostmanDubboRequest>

    fun getRegisterFactory(zk: String): RegisterFactory {
        val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk)
        return getRegisterFactory(type!!)
    }

    fun getInvoker(cluster: String): Invoker<Any, PostmanDubboRequest> {
        val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, cluster)
        val centerType = getByType(type)
        return when (centerType) {
            RegisterCenterType.ZK -> {
                dubboInvoker
            }

            RegisterCenterType.EUREKA -> {
                feignInvoker
            }

            else -> {
                throw RuntimeException("wrong type:$type")
            }
        }
    }

    fun getType(cluster: String): RegisterCenterType? {
        val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, cluster)
        return getByType(type)
    }

    fun getCreator(zk: String): Creator {
        val type = redisRepository.mapGet<Int>(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk)
        val registrationCenterType = getByType(type)
        if (registrationCenterType == RegisterCenterType.ZK) {
            return SpringUtils.context.getBean(DubboCreator::class.java)
        } else if (registrationCenterType == RegisterCenterType.EUREKA) {
            return SpringUtils.context.getBean(EurekaCreator::class.java)
        }
        throw RuntimeException("wrong type:$type")
    }

    companion object {
        @JvmStatic
        fun getRegisterFactory(type: Int): RegisterFactory {
            val registrationCenterType = getByType(type)
            if (registrationCenterType == RegisterCenterType.ZK) {
                return SpringUtils.context.getBean(DubboRegisterFactory::class.java)
            } else if (registrationCenterType == RegisterCenterType.EUREKA) {
                return SpringUtils.context.getBean(EurekaRegisterFactory::class.java)
            }
            throw RuntimeException("wrong type:$type")
        }
    }
}
