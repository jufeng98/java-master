package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 提供一些公共的模板方法
 *
 * @author yudong
 */
@Service
public abstract class AbstractController {

    Map<String, String> getAllSimpleClassName(String zk, String serviceName) {
        String modelKey = BuildUtils.buildServiceKey(zk, serviceName);
        Map<String, String> interfaceMap = new LinkedHashMap<>(10);
        PostmanService postmanService = InvokeContext.getService(modelKey);

        postmanService.getInterfaceModels().stream()
                .map(interfaceEntity -> {
                    String className = interfaceEntity.getInterfaceName();
                    String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
                    return Pair.of(simpleClassName, interfaceEntity.getKey());
                })
                .sorted(Comparator.comparing(Pair::getLeft))
                .forEach(pair -> interfaceMap.put(pair.getLeft(), pair.getRight()));
        return interfaceMap;
    }
}
