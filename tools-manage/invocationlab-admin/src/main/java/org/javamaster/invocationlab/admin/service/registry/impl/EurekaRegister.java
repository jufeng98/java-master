package org.javamaster.invocationlab.admin.service.registry.impl;

import org.javamaster.invocationlab.admin.service.registry.Register;
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yudong
 */
public class EurekaRegister implements Register {
    private static final Map<String, List<String>> INSTANCES_CACHE = new ConcurrentHashMap<>();
    private final String cluster;
    private final RestTemplate restTemplate;

    public EurekaRegister(String cluster, RestTemplate restTemplate) {
        this.cluster = cluster;
        this.restTemplate = restTemplate;
    }

    public static void clearCache() {
        INSTANCES_CACHE.clear();
    }

    @Override
    public void pullData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, InterfaceMetaInfo>> getAllService() {
        Map<String, Map<String, InterfaceMetaInfo>> map = Maps.newHashMap();
        JsonNode jsonNode = restTemplate.getForObject("http://" + cluster + "/eureka/apps/", JsonNode.class);
        @SuppressWarnings({"DataFlowIssue", "ConstantConditions"})
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("applications").get("application");
        for (JsonNode node : arrayNode) {
            map.put(node.get("name").asText(), Collections.emptyMap());
        }
        return map;
    }

    @Override
    public List<String> getServiceInstances(String serviceName) {
        List<String> instances = INSTANCES_CACHE.get(serviceName);
        if (instances != null) {
            return instances;
        }
        instances = Lists.newArrayList();
        JsonNode jsonNode = restTemplate.getForObject("http://" + cluster + "/eureka/apps/" + serviceName, JsonNode.class);
        @SuppressWarnings({"DataFlowIssue", "ConstantConditions"})
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("application").get("instance");
        for (JsonNode node : arrayNode) {
            instances.add(node.get("ipAddr").asText() + ":" + node.get("port").get("$").asText());
        }
        INSTANCES_CACHE.put(serviceName, instances);
        return instances;
    }
}
