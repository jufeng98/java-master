package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.enums.RegisterCenterType;
import org.javamaster.invocationlab.admin.service.creation.Creator;
import org.javamaster.invocationlab.admin.service.creation.impl.DubboCreator;
import org.javamaster.invocationlab.admin.service.creation.impl.EurekaCreator;
import org.javamaster.invocationlab.admin.service.invocation.Invoker;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory;
import org.javamaster.invocationlab.admin.service.registry.impl.DubboRegisterFactory;
import org.javamaster.invocationlab.admin.service.registry.impl.EurekaRegisterFactory;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 * @date 2022/11/12
 */
@Component
public class AppFactory {
    @Autowired
    private RedisRepository redisRepository;
    @Autowired
    private Invoker<Object, PostmanDubboRequest> dubboInvoker;
    @Autowired
    @Qualifier("feignInvoker")
    private Invoker<Object, PostmanDubboRequest> feignInvoker;

    public static RegisterFactory getRegisterFactory(Integer type) {
        RegisterCenterType registrationCenterType = RegisterCenterType.getByType(type);
        if (registrationCenterType == RegisterCenterType.ZK) {
            return SpringUtils.getContext().getBean(DubboRegisterFactory.class);
        } else if (registrationCenterType == RegisterCenterType.EUREKA) {
            return SpringUtils.getContext().getBean(EurekaRegisterFactory.class);
        }
        throw new RuntimeException("wrong type:" + type);
    }

    public RegisterFactory getRegisterFactory(String zk) {
        Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk);
        return getRegisterFactory(type);
    }

    public Invoker<Object, PostmanDubboRequest> getInvoker(String cluster) {
        Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, cluster);
        RegisterCenterType centerType = RegisterCenterType.getByType(type);
        if (centerType == RegisterCenterType.ZK) {
            return dubboInvoker;
        } else if (centerType == RegisterCenterType.EUREKA) {
            return feignInvoker;
        } else {
            throw new RuntimeException("wrong type:" + type);
        }
    }

    public RegisterCenterType getType(String cluster) {
        Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, cluster);
        return RegisterCenterType.getByType(type);
    }

    public Creator getCreator(String zk) {
        Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk);
        RegisterCenterType registrationCenterType = RegisterCenterType.getByType(type);
        if (registrationCenterType == RegisterCenterType.ZK) {
            return SpringUtils.getContext().getBean(DubboCreator.class);
        } else if (registrationCenterType == RegisterCenterType.EUREKA) {
            return SpringUtils.getContext().getBean(EurekaCreator.class);
        }
        throw new RuntimeException("wrong type:" + type);
    }
}
