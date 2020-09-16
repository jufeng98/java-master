package org.javamaster.b2c.test.utils;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * @author yudong
 * @date 2019/6/13
 */
public class DubboUtils {
    /**
     * dubbo服务的zookeeper注册中心地址
     */
    private static final String ZOOKEEPER_ADDRESS = "zookeeper://127.0.0.1:2181";

    public static <T> T getService(Class<T> dubboServiceClass, String version) {
        return getService(dubboServiceClass, version, null);
    }

    @SuppressWarnings("ALL")
    public static <T> T getService(Class<T> dubboServiceClass, String version, String host) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(dubboServiceClass);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            ReferenceConfig<GenericService> reference = getReferenceConfig(dubboServiceClass, version, host);
            GenericService genericService = reference.get();
            return genericService.$invoke(method.getName(), getMethodParamType(method), args);
        });
        Object service = enhancer.create();
        return (T) service;
    }

    private static <T> ReferenceConfig<T> getReferenceConfig(Class<?> interfaceClass, String version, String host) {
        String referenceKey = interfaceClass.getName();
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(getApplicationConfig());
        referenceConfig.setRegistry(getRegistryConfig());
        referenceConfig.setInterface(interfaceClass);
        referenceConfig.setVersion(version);
        referenceConfig.setTimeout(8000);
        referenceConfig.setGeneric(true);
        if (StringUtils.isNotBlank(host)) {
            String url = String.format("dubbo://%s/%s", host, referenceKey);
            referenceConfig.setUrl(url);
        }
        return referenceConfig;
    }

    private static RegistryConfig getRegistryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(DubboUtils.ZOOKEEPER_ADDRESS);
        return registryConfig;
    }

    private static ApplicationConfig getApplicationConfig() {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("b2c-test");
        return application;
    }

    private static String[] getMethodParamType(Method method) {
        Class<?>[] paramClassList = method.getParameterTypes();
        String[] paramTypeList = new String[paramClassList.length];
        for (int i = 0; i < paramClassList.length; i++) {
            paramTypeList[i] = paramClassList[i].getTypeName();
        }
        return paramTypeList;
    }
}
