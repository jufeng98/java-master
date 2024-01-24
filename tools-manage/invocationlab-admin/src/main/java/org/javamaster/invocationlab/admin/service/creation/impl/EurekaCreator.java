package org.javamaster.invocationlab.admin.service.creation.impl;

import org.javamaster.invocationlab.admin.consts.Constant;
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
import org.javamaster.invocationlab.admin.util.JsonUtils;
import com.google.common.collect.Lists;
import lombok.Cleanup;
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
        @Cleanup
        ApiJarClassLoader tmpLoader = JarLocalFileLoader.initClassLoader(serviceName, gav.getVersion());
        List<Class<?>> feignClientClasses = findAllFeignClientClassFromLibJars(serviceName, gav, tmpLoader);
        List<String> feignServicePackages = findFeignServicePackages(feignClientClasses);
        // 获取所有带有FeignClient注解的接口里的所有方法
        List<InterfaceEntity> list = getAllFeignClientMethods(feignClientClasses);

        postmanService.getInterfaceModels().addAll(list);

        saveToRedisAndInitLoader(postmanService);

        String serviceKey = BuildUtils.buildServiceKey(postmanService.getCluster(), postmanService.getServiceName());
        ApiJarClassLoader apiJarClassLoader = JarLocalFileLoader.getAllClassLoader().get(serviceKey);
        // 将所有带有FeignClient注解的接口注册到Spring上下文中，以便于后续可以调用
        GenericApplicationContext context = FeignServiceRegistrar.register(feignServicePackages, apiJarClassLoader);
        InvokeContext.putFeignContext(serviceKey, context);

        return new Pair<>(true, "成功");
    }

    private List<String> findFeignServicePackages(List<Class<?>> feignClientClasses) {
        return feignClientClasses.stream()
                .map(it -> it.getPackage().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Pair<Boolean, String> refresh(String cluster, String serviceName) {
        EurekaRegister.clearCache();
        String serviceKey = BuildUtils.buildServiceKey(cluster, serviceName);
        Object serviceObj = redisRepository.mapGet(RedisKeys.RPC_MODEL_KEY, serviceKey);

        PostmanService postmanService = JsonUtils.parseObject((String) serviceObj, DubboPostmanService.class);
        GAV gav = postmanService.getGav();

        String oldInfo = gav.toString();
        upgradeGavVersionToLatest(gav);
        String newInfo = gav.toString();

        Pair<Boolean, String> pair = create(cluster, gav, serviceName);

        return new Pair<>(pair.getLeft(), "刷新服务 " + oldInfo + " => " + newInfo + " 成功!");
    }

    public void initFeignInfoAndPutContext(PostmanService service, String serviceKey) {
        ApiJarClassLoader apiJarClassLoader = JarLocalFileLoader.getAllClassLoader().get(serviceKey);

        List<Class<?>> feignClientClasses = findAllFeignClientClassFromLibJars(service.getServiceName(), service.getGav(),
                apiJarClassLoader);
        List<String> feignServicePackages = findFeignServicePackages(feignClientClasses);

        GenericApplicationContext context = FeignServiceRegistrar.register(feignServicePackages, apiJarClassLoader);
        InvokeContext.putFeignContext(serviceKey, context);
    }

    @SneakyThrows
    private List<Class<?>> findAllFeignClientClassFromLibJars(String serviceName, GAV gav, ApiJarClassLoader apiJarClassLoader) {
        String apiFilePath = JarLocalFileLoader.getApiFilePath(serviceName, gav);

        @Cleanup
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
                        logger.error("get method error:{}", aClass, e);
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
