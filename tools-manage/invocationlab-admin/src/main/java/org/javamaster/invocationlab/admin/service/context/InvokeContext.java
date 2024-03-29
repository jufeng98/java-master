package org.javamaster.invocationlab.admin.service.context;

import org.javamaster.invocationlab.admin.enums.RegisterCenterType;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam;
import org.javamaster.invocationlab.admin.service.creation.impl.EurekaCreator;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboInvocation;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yudong
 */
public class InvokeContext {

    private static final Map<String, GenericApplicationContext> CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final Map<String, PostmanService> POSTMAN_SERVICE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, List<RequestParam>> REQUESTPARAM_MAP = new ConcurrentHashMap<>();

    public static PostmanService getService(String serviceKey) {
        return POSTMAN_SERVICE_MAP.getOrDefault(serviceKey, null);
    }

    public static List<RequestParam> getRequestParam(String methodNameKey) {
        return REQUESTPARAM_MAP.getOrDefault(methodNameKey, null);
    }

    public static void putService(String serviceKey, PostmanService service) {
        POSTMAN_SERVICE_MAP.put(serviceKey, service);
    }

    public static void removeService(String serviceKey) {
        POSTMAN_SERVICE_MAP.remove(serviceKey);
    }

    public static void putFeignContext(String serviceKey, GenericApplicationContext context) {
        CONTEXT_MAP.put(serviceKey, context);
    }

    public static GenericApplicationContext getFeignContext(String serviceKey) {
        return CONTEXT_MAP.get(serviceKey);
    }

    public static void putMethod(String methodKey, List<RequestParam> requestParamList) {
        REQUESTPARAM_MAP.put(methodKey, requestParamList);
    }

    public static void tryLoadRuntimeInfo(PostmanService postmanService, RegisterCenterType type, String serviceKey) {
        if (postmanService == null) {
            return;
        }

        if (postmanService.getLoadedToClassLoader()) {
            return;
        }

        //服务重启的时候需要重新构建运行时信息
        JarLocalFileLoader.loadInterfaceInfoAndInitLoader(postmanService);

        if (type == RegisterCenterType.EUREKA) {
            EurekaCreator creator = SpringUtils.getContext().getBean(EurekaCreator.class);
            creator.initFeignInfoAndPutContext(postmanService, serviceKey);
        }
    }

    public static Pair<PostmanDubboRequest, Invocation> buildInvocation(String cluster,
                                                                        String serviceName,
                                                                        String interfaceKey,
                                                                        String methodName,
                                                                        String dubboParam,
                                                                        String dubboIp) {
        PostmanDubboRequest request = new PostmanDubboRequest();
        request.setCluster(cluster);
        request.setServiceName(serviceName);
        String group = BuildUtils.getGroupByInterfaceKey(interfaceKey);
        request.setGroup(group);
        String interfaceName = BuildUtils.getInterfaceNameByInterfaceKey(interfaceKey);
        request.setInterfaceName(interfaceName);
        String version = BuildUtils.getVersionByInterfaceKey(interfaceKey);
        request.setVersion(version);
        request.setMethodName(methodName);
        request.setDubboParam(dubboParam);
        request.setDubboIp(dubboIp);

        Invocation invocation = new DubboInvocation();
        String javaMethodName = BuildUtils.getJavaMethodName(methodName);
        invocation.setJavaMethodName(javaMethodName);
        String methodNameKey = BuildUtils.getMethodNameKey(cluster, serviceName, interfaceKey, methodName);
        List<RequestParam> requestParamList = InvokeContext.getRequestParam(methodNameKey);
        invocation.setRequestParams(requestParamList);

        return new Pair<>(request, invocation);
    }
}
