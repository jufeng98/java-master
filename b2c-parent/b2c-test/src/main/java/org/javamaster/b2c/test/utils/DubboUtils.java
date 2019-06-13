package org.javamaster.b2c.test.utils;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSONObject;
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
    // private static final String address = "zookeeper://127.0.0.1:2181";
    private static final String address = "zookeeper://192.168.240.15:2181";

    public static <T> T getService(Class<T> clazz, String version, String hostPort) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            ReferenceConfig<GenericService> reference = getReferenceConfig(clazz, null, version, hostPort);
            GenericService genericService = reference.get();
            Object result = genericService.$invoke(method.getName(), getMethodParamType(method), args);
            String resJsonStr = JSONObject.toJSONString(result);
            return JSONObject.parseObject(resJsonStr, method.getReturnType());
        });
        Object service = enhancer.create();
        return (T) service;
    }

    private static ReferenceConfig getReferenceConfig(Class<?> interfaceClass, String group, String version,
                                                      String hostPort) {
        String referenceKey = interfaceClass.getName();
        ReferenceConfig referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(application);
        referenceConfig.setRegistry(getRegistryConfig(address, group, version));
        referenceConfig.setInterface(interfaceClass);
        referenceConfig.setVersion(version);
        referenceConfig.setTimeout(30000);
        referenceConfig.setGeneric(true);
        if (hostPort != null) {
            String url = String.format("dubbo://%s/%s", hostPort, referenceKey);
            referenceConfig.setUrl(url);
        }
        return referenceConfig;
    }

    private static RegistryConfig getRegistryConfig(String address, String group, String version) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(address);
        registryConfig.setVersion(version);
        registryConfig.setGroup(group);
        registryConfig.setTimeout(30000);
        return registryConfig;
    }

    public static String[] getMethodParamType(Method method) {
        Class[] paramClassList = method.getParameterTypes();
        String[] paramTypeList = new String[paramClassList.length];
        for (int i = 0; i < paramClassList.length; i++) {
            paramTypeList[i] = paramClassList[i].getTypeName();
        }
        return paramTypeList;
    }
}
