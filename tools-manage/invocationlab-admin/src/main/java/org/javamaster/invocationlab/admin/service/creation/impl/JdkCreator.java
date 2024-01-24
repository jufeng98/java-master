package org.javamaster.invocationlab.admin.service.creation.impl;

import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.creation.AbstractCreator;
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author yudong
 */
@Slf4j
@Component
public class JdkCreator extends AbstractCreator {
    public static ApiJarClassLoader globalJdkClassLoader;

    @Override
    public Pair<Boolean, String> create(String cluster, GAV gav, String serviceName) {
        DubboPostmanService service = new DubboPostmanService();
        service.setServiceName(serviceName);
        service.setGav(gav);
        service.setGenerateTime(System.currentTimeMillis());

        return doCreateService(service);
    }

    @SneakyThrows
    public static void initGlobalJdkClassLoader() {
        RedisRepository redisRepository = SpringUtils.getContext().getBean(RedisRepository.class);
        Set<Object> serviceNameSet = redisRepository.members(RedisKeys.RPC_MODEL_JDK_KEY);

        URL[] urls = serviceNameSet.stream()
                .map(serviceName -> (String) redisRepository.mapGet(RedisKeys.RPC_MODEL_JDK_MAP_KEY, serviceName))
                .filter(Objects::nonNull)
                .map(serviceJson -> {
                    DubboPostmanService service = JsonUtils.parseObject(serviceJson, DubboPostmanService.class);
                    String jarLibPath = JarLocalFileLoader.getJarLibPath(service.getServiceName(), service.getGav().getVersion());
                    return JarLocalFileLoader.getJarLibUrls(jarLibPath);
                })
                .flatMap(Collection::stream)
                .toArray(URL[]::new);

        globalJdkClassLoader = new ApiJarClassLoader(urls);
        log.info("共引入{}个jar创建redis新加载器:{}", urls.length, globalJdkClassLoader.getClass());
    }

    @Override
    @SneakyThrows
    public Pair<Boolean, String> refresh(String cluster, String serviceName) {
        globalJdkClassLoader.close();
        log.info("完成销毁redis旧加载器:{}", globalJdkClassLoader.getClass());

        String serviceJson = redisRepository.mapGet(RedisKeys.RPC_MODEL_JDK_MAP_KEY, serviceName);
        DubboPostmanService service = JsonUtils.parseObject(serviceJson, DubboPostmanService.class);

        upgradeGavVersionToLatest(service.getGav());

        String oldGavVersion = ThreadLocalUtils.get(ThreadLocalUtils.OLD_GAV_VERSION);
        if (StringUtils.isNotBlank(oldGavVersion)) {
            String jarLibPath = JarLocalFileLoader.getJarLibPath(serviceName, oldGavVersion);
            File parentFile = new File(jarLibPath).getParentFile();
            FileUtils.deleteDirectory(parentFile);
            ThreadLocalUtils.remove(ThreadLocalUtils.OLD_GAV_VERSION);
            logger.info("完成删除redis旧版本服务目录:{}", parentFile);
        }

        return doCreateService(service);
    }

    private Pair<Boolean, String> doCreateService(PostmanService service) {
        GAV gav = service.getGav();

        String serviceName = service.getServiceName();
        redisRepository.setAdd(RedisKeys.RPC_MODEL_JDK_KEY, serviceName);

        resolveMavenDependencies(serviceName, gav);

        String serviceJson = JsonUtils.objectToString(service);
        redisRepository.mapPut(RedisKeys.RPC_MODEL_JDK_MAP_KEY, serviceName, serviceJson);

        initGlobalJdkClassLoader();

        return new Pair<>(true, "解析" + gav + "依赖成功!!!");
    }

}
