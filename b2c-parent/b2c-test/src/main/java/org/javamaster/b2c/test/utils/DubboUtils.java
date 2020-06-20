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
    private static ApplicationConfig application = new ApplicationConfig();

    static {
        application.setName("b2c-test");
    }

    /**
     * dubbo服务的zookeeper注册中心地址
     */
    // private static final String ZOOKEEPER_ADDRESS = "zookeeper://127.0.0.1:2181";
    private static final String ZOOKEEPER_ADDRESS = "zookeeper://192.168.240.15:2181";

    public static <T> T getService(Class<T> dubboServiceClass, String version) {
        return getService(dubboServiceClass, null, version, null);
    }

    public static <T> T getService(Class<T> dubboServiceClass, String version, String host) {
        return getService(dubboServiceClass, null, version, host);
    }

    @SuppressWarnings("ALL")
    public static <T> T getService(Class<T> dubboServiceClass, String group, String version, String host) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(dubboServiceClass);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            ReferenceConfig<GenericService> reference = getReferenceConfig(dubboServiceClass, group, version, host);
            GenericService genericService = reference.get();
            Object result = genericService.$invoke(method.getName(), getMethodParamType(method), args);
            String resJsonStr = OMUtils.getInstance().writeValueAsString(result);
            return OMUtils.getInstance().readValue(resJsonStr, method.getReturnType());
        });
        Object service = enhancer.create();
        return (T) service;
    }

    private static <T> ReferenceConfig<T> getReferenceConfig(Class<?> interfaceClass, String group, String version,
                                                             String host) {
        String referenceKey = interfaceClass.getName();
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(application);
        referenceConfig.setRegistry(getRegistryConfig(group, version));
        referenceConfig.setInterface(interfaceClass);
        referenceConfig.setVersion(version);
        referenceConfig.setTimeout(30000);
        referenceConfig.setGeneric(true);
        if (StringUtils.isNotBlank(host)) {
            String url = String.format("dubbo://%s/%s", host, referenceKey);
            referenceConfig.setUrl(url);
        }
        return referenceConfig;
    }

    private static RegistryConfig getRegistryConfig(String group, String version) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(DubboUtils.ZOOKEEPER_ADDRESS);
        registryConfig.setVersion(version);
        registryConfig.setGroup(group);
        registryConfig.setTimeout(30000);
        return registryConfig;
    }

    private static String[] getMethodParamType(Method method) {
        Class[] paramClassList = method.getParameterTypes();
        String[] paramTypeList = new String[paramClassList.length];
        for (int i = 0; i < paramClassList.length; i++) {
            paramTypeList[i] = paramClassList[i].getTypeName();
        }
        return paramTypeList;
    }
}
