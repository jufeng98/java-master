package org.javamaster.invocationlab.admin.service.creation;

import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader;
import org.javamaster.invocationlab.admin.service.maven.Maven;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author yudong
 * @date 2022/11/13
 */
public abstract class AbstractCreator implements Creator {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected RedisRepository redisRepository;
    @Autowired
    protected Maven maven;
    @Autowired
    protected RestTemplate restTemplate;
    @Value("${nexus.url.search}")
    private String nexusPath;

    protected void resolveMavenDependencies(String serviceName, GAV gav) {
        logger.info("开始创建服务...");
        logger.info("如果系统是第一次构建服务则需要下载各种maven plugin,耗时比较长");
        maven.dependency(serviceName, gav);
    }

    protected void saveRedisAndLoad(PostmanService postmanService) {
        String serviceString = JsonUtils.objectToString(postmanService);
        String serviceKey = BuildUtils.buildServiceKey(postmanService.getCluster(), postmanService.getServiceName());
        redisRepository.removeMap(RedisKeys.RPC_MODEL_KEY, serviceKey);
        redisRepository.mapPut(RedisKeys.RPC_MODEL_KEY, serviceKey, serviceString);
        redisRepository.setAdd(postmanService.getCluster(), postmanService.getServiceName());

        JarLocalFileLoader.loadRuntimeInfo(postmanService);
        InvokeContext.putService(serviceKey, postmanService);
    }

    protected void upgradeGavVersionToLatest(GAV gav) {
        JSONObject jsonObject = restTemplate.getForObject(nexusPath + "?g={g}&a={a}&p=jar&collapseresults=true",
                JSONObject.class, gav.getGroupID(), gav.getArtifactID());
        @SuppressWarnings("DataFlowIssue")
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> gavInfo = (Map<String, Object>) jsonArray.get(0);
        String latestVersion;
        if (gav.getVersion().contains("SNAPSHOT")) {
            latestVersion = (String) gavInfo.get("latestSnapshot");
        } else {
            latestVersion = (String) gavInfo.get("latestRelease");
        }
        gav.setVersion(latestVersion);
    }

    @Override
    public String getLatestVersion(GAV gav) {
        JSONObject jsonObject = restTemplate.getForObject(nexusPath + "?g={g}&a={a}&p=jar&collapseresults=true",
                JSONObject.class, gav.getGroupID(), gav.getArtifactID());
        @SuppressWarnings("DataFlowIssue")
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> gavInfo = (Map<String, Object>) jsonArray.get(0);
        String latestVersion;
        if (StringUtils.isBlank(gav.getVersion())) {
            latestVersion = (String) gavInfo.get("latestSnapshot");
            if (StringUtils.isBlank(latestVersion)) {
                latestVersion = (String) gavInfo.get("latestRelease");
            }
            return latestVersion;
        }
        if (gav.getVersion().contains("SNAPSHOT")) {
            latestVersion = (String) gavInfo.get("latestSnapshot");
        } else {
            latestVersion = (String) gavInfo.get("latestRelease");
        }
        return latestVersion;
    }
}
