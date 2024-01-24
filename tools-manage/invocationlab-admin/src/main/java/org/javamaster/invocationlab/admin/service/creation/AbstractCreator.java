package org.javamaster.invocationlab.admin.service.creation;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader;
import org.javamaster.invocationlab.admin.service.maven.Maven;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;

/**
 * @author yudong
 * @date 2022/11/13
 */
@SuppressWarnings("VulnerableCodeUsages")
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

    protected void saveToRedisAndInitLoader(PostmanService postmanService) {
        String serviceString = JsonUtils.objectToString(postmanService);
        String serviceKey = BuildUtils.buildServiceKey(postmanService.getCluster(), postmanService.getServiceName());

        redisRepository.removeMap(RedisKeys.RPC_MODEL_KEY, serviceKey);
        redisRepository.mapPut(RedisKeys.RPC_MODEL_KEY, serviceKey, serviceString);
        redisRepository.setAdd(postmanService.getCluster(), postmanService.getServiceName());

        JarLocalFileLoader.loadInterfaceInfoAndInitLoader(postmanService);
        InvokeContext.putService(serviceKey, postmanService);
    }

    @SneakyThrows
    public void remove(String cluster, String serviceName) {
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);

        String serviceObj = redisRepository.mapGet(RedisKeys.RPC_MODEL_KEY, serviceKey);
        PostmanService postmanService = JsonUtils.parseObject(serviceObj, DubboPostmanService.class);

        String jarLibPath =JarLocalFileLoader.getJarLibPath(serviceName, postmanService.getGav().getVersion());
        File parentFile = new File(jarLibPath).getParentFile();
        FileUtils.deleteDirectory(parentFile);
        logger.info("完成删除版本服务目录:{}", parentFile);

        redisRepository.removeMap(RedisKeys.RPC_MODEL_KEY, serviceKey);
        redisRepository.setRemove(cluster, serviceKey);
        InvokeContext.removeService(serviceKey);
        logger.info("完成删除redis服务相关信息:{}", serviceKey);
    }

    @SneakyThrows
    protected void upgradeGavVersionToLatest(GAV gav) {
        String version = gav.getVersion();

        JSONObject jsonObject = restTemplate.getForObject(nexusPath + "?g={g}&a={a}&collapseresults=true",
                JSONObject.class, gav.getGroupID(), gav.getArtifactID());
        @SuppressWarnings("DataFlowIssue")
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> gavInfo = (Map<String, Object>) jsonArray.get(0);
        String latestVersion;
        if (version.contains("SNAPSHOT")) {
            latestVersion = (String) gavInfo.get("latestSnapshot");
        } else {
            latestVersion = (String) gavInfo.get("latestRelease");
        }
        gav.setVersion(latestVersion);

        if (!version.equals(latestVersion)) {
            ThreadLocalUtils.set(ThreadLocalUtils.OLD_GAV_VERSION, version);
        }
    }

    @Override
    public String getLatestVersion(GAV gav) {
        JSONObject jsonObject = restTemplate.getForObject(nexusPath + "?g={g}&a={a}&collapseresults=true",
                JSONObject.class, gav.getGroupID(), gav.getArtifactID());
        @SuppressWarnings("DataFlowIssue")
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        if (jsonArray.isEmpty()) {
            throw new BizException(gav + "依赖不存在!!!");
        }
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
