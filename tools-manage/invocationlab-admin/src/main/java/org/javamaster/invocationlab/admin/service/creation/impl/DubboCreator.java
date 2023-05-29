package org.javamaster.invocationlab.admin.service.creation.impl;

import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.creation.AbstractCreator;
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;
import org.javamaster.invocationlab.admin.service.registry.impl.DubboRegisterFactory;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * @author yudong
 */
@Component
public class DubboCreator extends AbstractCreator {

    @Autowired
    private DubboRegisterFactory dubboRegisterFactory;

    @Override
    public Pair<Boolean, String> create(String cluster, GAV gav, String serviceName) {
        Map<String, InterfaceMetaInfo> providers = dubboRegisterFactory.get(cluster)
                .getAllService().get(serviceName);
        DubboPostmanService dubboPostmanService = new DubboPostmanService();
        dubboPostmanService.setCluster(cluster);
        dubboPostmanService.setServiceName(serviceName);

        for (Map.Entry<String, InterfaceMetaInfo> entry : providers.entrySet()) {
            InterfaceEntity dubboInterfaceModel = new InterfaceEntity();
            String providerName = entry.getValue().getInterfaceName();
            String version = entry.getValue().getVersion();
            Set<String> serverIps = entry.getValue().getServerIps();
            Set<String> methodNames = entry.getValue().getMethodNames();
            dubboInterfaceModel.setKey(entry.getKey());
            dubboInterfaceModel.setInterfaceName(providerName);
            dubboInterfaceModel.setMethodNames(methodNames);
            dubboInterfaceModel.setServerIps(serverIps);
            dubboInterfaceModel.setVersion(version);
            dubboInterfaceModel.setGroup(entry.getValue().getGroup());
            dubboPostmanService.getInterfaceModels().add(dubboInterfaceModel);
        }
        dubboPostmanService.setGav(gav);
        dubboPostmanService.setGenerateTime(System.currentTimeMillis());
        return doCreateService(serviceName, dubboPostmanService);
    }

    @Override
    public Pair<Boolean, String> refresh(String cluster, String serviceName) {
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);
        Object serviceObj = redisRepository.mapGet(RedisKeys.RPC_MODEL_KEY, serviceKey);
        PostmanService postmanService = JsonUtils.parseObject((String) serviceObj, DubboPostmanService.class);
        GAV gav = postmanService.getGav();
        String oldInfo = gav.getGroupID() + ":" + gav.getArtifactID() + ":" + gav.getVersion();
        upgradeGavVersionToLatest(gav);
        String newInfo = gav.getGroupID() + ":" + gav.getArtifactID() + ":" + gav.getVersion();
        Pair<Boolean, String> pair = create(cluster, gav, serviceName);
        return new Pair<>(pair.getLeft(), "刷新服务 " + oldInfo + " => " + newInfo + " 成功!");
    }

    private Pair<Boolean, String> doCreateService(String serviceName,
                                                  PostmanService postmanService) {
        GAV gav = postmanService.getGav();
        resolveMavenDependencies(serviceName, gav);
        saveRedisAndLoad(postmanService);
        return new Pair<>(true, gav.toString());
    }

}
