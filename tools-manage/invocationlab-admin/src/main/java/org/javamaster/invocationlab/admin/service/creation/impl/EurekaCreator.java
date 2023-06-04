package org.javamaster.invocationlab.admin.service.creation.impl;

import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.AbstractCreator;
import org.javamaster.invocationlab.admin.service.creation.entity.DubboPostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader;
import org.javamaster.invocationlab.admin.service.registry.impl.EurekaRegister;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.Constant;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignServiceRegistrar;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
@Component
public class EurekaCreator extends AbstractCreator {

    @SneakyThrows
    @Override
    public Pair<Boolean, String> create(String cluster, GAV gav, String serviceName) {
        resolveMavenDependencies(serviceName, gav);
        DubboPostmanService postmanService = new DubboPostmanService();
        postmanService.setCluster(cluster);
        postmanService.setServiceName(serviceName);
        postmanService.setGav(gav);
        postmanService.setGenerateTime(System.currentTimeMillis());

        // 找到api包里所有带有FeignClient注解的接口
        List<Class<?>> feignClientClasses = loadAllFeignClientClass(serviceName, gav);
        // 获取所有带有FeignClient注解的接口里的所有方法
        List<InterfaceEntity> list = getAllFeignClientMethods(feignClientClasses);
        postmanService.getInterfaceModels().addAll(list);

        saveRedisAndLoad(postmanService);

        String serviceKey = BuildUtils.buildServiceKey(postmanService.getCluster(), postmanService.getServiceName());
        // 将所有带有FeignClient注解的接口注册到Spring上下文中，以便于后续可以调用
        GenericApplicationContext context = FeignServiceRegistrar.register(gav, JarLocalFileLoader.getAllClassLoader().get(serviceKey));
        InvokeContext.putContext(serviceKey, context);
        return new Pair<>(true, "成功");
    }

    @Override
    public Pair<Boolean, String> refresh(String cluster, String serviceName) {
        EurekaRegister.clearCache();
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

    public GenericApplicationContext initAndPutContext(String cluster, String serviceName) {
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);
        PostmanService service = InvokeContext.getService(serviceKey);
        GenericApplicationContext context = FeignServiceRegistrar.register(service.getGav(),
                JarLocalFileLoader.getAllClassLoader().get(serviceKey));
        InvokeContext.putContext(serviceKey, context);
        return context;
    }

    @SneakyThrows
    private List<Class<?>> loadAllFeignClientClass(String serviceName, GAV gav) {
        ApiJarClassLoader apiJarClassLoader = JarLocalFileLoader.initClassLoader(serviceName, gav.getVersion());
        String apiFilePath = JarLocalFileLoader.getApiFilePath(serviceName, gav);
        JarFile jarFile = new JarFile(apiFilePath);
        Enumeration<JarEntry> enumeration = jarFile.entries();
        List<Class<?>> list = Lists.newArrayList();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement();
            String name = jarEntry.getName();
            if (!name.endsWith(".class")) {
                continue;
            }
            name = name.replace(".class", "").replace("/", ".");
            Class<?> aClass;
            try {
                aClass = apiJarClassLoader.loadClassWithResolve(name);
            } catch (Throwable e) {
                logger.error("load error:{}", name, e);
                continue;
            }
            if (!aClass.isInterface()) {
                continue;
            }
            FeignClient feignClient = aClass.getAnnotation(FeignClient.class);
            if (feignClient == null) {
                continue;
            }
            list.add(aClass);
        }
        jarFile.close();
        apiJarClassLoader.close();
        return list;
    }

    public List<InterfaceEntity> getAllFeignClientMethods(List<Class<?>> classes) {
        return classes.stream()
                .map(aClass -> {
                    InterfaceEntity interfaceModel = new InterfaceEntity();
                    String providerName = aClass.getName();
                    Set<String> methodNames;
                    try {
                        methodNames = Arrays.stream(aClass.getDeclaredMethods())
                                .map(Method::getName)
                                .collect(Collectors.toSet());
                    } catch (Throwable e) {
                        logger.error("get method error:{},error:{}", aClass, e);
                        return null;
                    }
                    interfaceModel.setInterfaceName(providerName);
                    interfaceModel.setMethodNames(methodNames);
                    interfaceModel.setServerIps(Collections.emptySet());
                    interfaceModel.setVersion(Constant.DEFAULT_VERSION);
                    interfaceModel.setGroup(Constant.GROUP_DEFAULT);
                    interfaceModel.setKey(BuildUtils.buildInterfaceKey(interfaceModel.getGroup(), providerName, interfaceModel.getVersion()));
                    return interfaceModel;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
