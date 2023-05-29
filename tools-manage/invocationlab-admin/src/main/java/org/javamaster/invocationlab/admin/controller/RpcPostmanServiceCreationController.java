package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.service.AppFactory;
import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.creation.Creator;
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory;
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;
import org.javamaster.invocationlab.admin.util.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 提供服务的创建及刷新的功能
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanServiceCreationController {

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
        Map<String, String> dm = XmlUtils.parseDependencyXml(dependency);
        if (dm.size() < 2) {
            return WebApiRspDto.error("dependency格式不对,请指定正确的maven dependency,区分大小写");
        }
        String g = dm.get("groupId");
        String a = dm.get("artifactId");
        String v = dm.get("version");

        GAV gav = new GAV();
        gav.setGroupID(g);
        gav.setArtifactID(a);
        if (StringUtils.isBlank(v)) {
            Creator creator = appFactory.getCreator(zk);
            String latestVersion = creator.getLatestVersion(gav);
            gav.setVersion(latestVersion);
        } else {
            gav.setVersion(v);
        }
        Creator creator = appFactory.getCreator(zk);
        Pair<Boolean, String> pair = creator.create(zk, gav, serviceName);
        if (!pair.getLeft()) {
            return WebApiRspDto.error(pair.getRight());
        }
        return WebApiRspDto.success(pair.getRight());
    }

    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    @ResponseBody
    public WebApiRspDto<String> refreshService(@RequestParam("zk") String zk,
                                               @RequestParam("zkServiceName") String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return WebApiRspDto.error("必须选择一个服务名称", -1);
        }
        Creator creator = appFactory.getCreator(zk);
        Pair<Boolean, String> pair = creator.refresh(zk, serviceName);
        if (!pair.getLeft()) {
            return WebApiRspDto.error(pair.getRight());
        }
        return WebApiRspDto.success(pair.getRight());
    }
}
