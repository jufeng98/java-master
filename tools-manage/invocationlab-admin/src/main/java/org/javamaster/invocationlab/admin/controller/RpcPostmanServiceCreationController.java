package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.model.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.AppFactory;
import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.Creator;
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.creation.impl.JdkCreator;
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory;
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.javamaster.invocationlab.admin.util.XmlUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.service.AppFactory.getRegisterFactory;

/**
 * 提供服务的创建及刷新的功能
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanServiceCreationController {
    @Autowired
    private RedisRepository redisRepository;
    @Autowired
    private AppFactory appFactory;

    /**
     * 根据zk,返回所有的应用名称,这个是zk里面的,还未创建
     */
    @RequestMapping(value = "result/appNames", method = {RequestMethod.GET})
    @ResponseBody
    public WebApiRspDto<Set<String>> getAppNames(@RequestParam("zk") String zk) {
        if (zk.isEmpty()) {
            return WebApiRspDto.error("zk地址必须指定");
        }
        RegisterFactory registerFactory = appFactory.getRegisterFactory(zk);
        Map<String, Map<String, InterfaceMetaInfo>> services = registerFactory.get(zk).getAllService();
        return WebApiRspDto.success(
                services.keySet().stream()
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> createService(@RequestParam("zk") String zk,
                                              @RequestParam("zkServiceName") String serviceName,
                                              @RequestParam("dependency") String dependency) {
        if (serviceName == null || serviceName.isEmpty()) {
            return WebApiRspDto.error("必须选择一个服务名称", -1);
        }
        if (dependency == null || dependency.isEmpty()) {
            return WebApiRspDto.error("dependency不能为空");
        }

        GAV gav = XmlUtils.parseGav(dependency);
        if (StringUtils.isBlank(gav.getVersion())) {
            Creator creator = appFactory.getCreator(zk);
            String latestVersion = creator.getLatestVersion(gav);
            gav.setVersion(latestVersion);
        }

        Creator creator = appFactory.getCreator(zk);
        Pair<Boolean, String> pair = creator.create(zk, gav, serviceName);
        if (!pair.getLeft()) {
            return WebApiRspDto.error(pair.getRight());
        }
        return WebApiRspDto.success(pair.getRight());
    }

    @RequestMapping(value = "createJdk", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> createJdk(@RequestParam("dependency") String dependency) {
        if (dependency == null || dependency.isEmpty()) {
            return WebApiRspDto.error("dependency不能为空");
        }

        GAV gav = XmlUtils.parseGav(dependency);

        JdkCreator creator = SpringUtils.getContext().getBean(JdkCreator.class);
        String latestVersion = creator.getLatestVersion(gav);
        gav.setVersion(latestVersion);

        Pair<Boolean, String> pair = creator.create("", gav, gav.getArtifactID());
        if (!pair.getLeft()) {
            return WebApiRspDto.error(pair.getRight());
        }
        return WebApiRspDto.success(pair.getRight());
    }

    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> refreshService(@RequestParam("zk") String zk,
                                               @RequestParam("zkServiceName") String serviceName,
                                               @RequestParam("dubbo") Boolean dubbo) {
        if (serviceName == null || serviceName.isEmpty()) {
            return WebApiRspDto.error("必须选择一个服务名称", -1);
        }
        if (dubbo) {
            String modelKey = BuildUtils.buildServiceKey(zk, serviceName);
            PostmanService postmanService = InvokeContext.getService(modelKey);
            List<String> names = postmanService.getInterfaceModels().stream()
                    .map(InterfaceEntity::getInterfaceName)
                    .collect(Collectors.toList());
            Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk);
            RegisterFactory registerFactory = getRegisterFactory(type);
            registerFactory.refreshService(names, zk);
        }

        Creator creator = appFactory.getCreator(zk);
        Pair<Boolean, String> pair = creator.refresh(zk, serviceName);

        if (!pair.getLeft()) {
            return WebApiRspDto.error(pair.getRight());
        }
        return WebApiRspDto.success(pair.getRight());
    }

    @RequestMapping(value = "refreshJdk", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> refreshServiceJdk(@RequestParam("dependencyName") String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return WebApiRspDto.error("必须选择一个依赖名称", -1);
        }
        JdkCreator creator = SpringUtils.getContext().getBean(JdkCreator.class);
        Pair<Boolean, String> pair = creator.refresh("", serviceName);
        if (!pair.getLeft()) {
            return WebApiRspDto.error(pair.getRight());
        }
        return WebApiRspDto.success(pair.getRight());
    }

    @RequestMapping(value = "delService", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> delService(@RequestParam("zkServiceName") String serviceName) {
        Set<String> zkAddrs = Sets.newHashSet();
        SpringUtils.getContext().getBeansOfType(RegisterFactory.class).values().stream()
                .map(RegisterFactory::getClusterSet)
                .forEach(zkAddrs::addAll);
        boolean del = false;
        for (String addr : zkAddrs) {
            Set<Object> serviceNameSet = redisRepository.members(addr);
            if (!serviceNameSet.contains(serviceName)) {
                continue;
            }
            del = true;
            redisRepository.setRemove(addr, serviceName);
            Creator creator = appFactory.getCreator(addr);
            creator.remove(addr, serviceName);
        }
        if (!del) {
            return WebApiRspDto.error("服务名有误");
        }
        return WebApiRspDto.success("删除服务成功");
    }
}
