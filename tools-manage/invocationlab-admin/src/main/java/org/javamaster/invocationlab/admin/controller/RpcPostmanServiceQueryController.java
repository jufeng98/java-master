package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.dto.WebApiRspDto;
import org.javamaster.invocationlab.admin.enums.RegisterCenterType;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.MethodEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.javamaster.invocationlab.admin.service.load.impl.JarLocalFileLoader;
import org.javamaster.invocationlab.admin.service.registry.Register;
import org.javamaster.invocationlab.admin.service.registry.RegisterFactory;
import org.javamaster.invocationlab.admin.service.registry.entity.InterfaceMetaInfo;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisKeys;
import org.javamaster.invocationlab.admin.service.repository.redis.RedisRepository;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.ClassUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.service.AppFactory.getRegisterFactory;

/**
 * 应用详细信息查询相关
 *
 * @author yudong
 */
@Controller
@RequestMapping("/dubbo-postman/")
public class RpcPostmanServiceQueryController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(RpcPostmanServiceQueryController.class);
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    private RedisRepository cacheService;

    @SneakyThrows
    private static Object newObj(Parameter parameter, ClassLoader classLoader) {
        Class<?> clazz = parameter.getType();
        if (Collection.class.isAssignableFrom(clazz)) {
            Type genericType = ClassUtils.getParamGenericType(parameter.getParameterizedType());
            Object obj;
            if (ClassUtils.shouldNew(genericType)) {
                Class<?> aClass = Class.forName(genericType.getTypeName(), true, classLoader);
                obj = newObj(aClass, classLoader, 1);
            } else {
                obj = null;
            }
            return List.class.isAssignableFrom(clazz) ?
                    Lists.newArrayList(obj) : Sets.newHashSet(obj);
        } else {
            if (ClassUtils.shouldNew(clazz)) {
                return newObj(clazz, classLoader, 1);
            } else {
                return null;
            }
        }
    }

    @SneakyThrows
    private static Object newObj(Class<?> clazz, ClassLoader classLoader, int deep) {
        Object object;
        try {
            if (clazz.isEnum()) {
                return null;
            } else {
                object = clazz.newInstance();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        if (deep >= 3) {
            return object;
        }
        for (Field declaredField : clazz.getDeclaredFields()) {
            Class<?> fieldType = declaredField.getType();
            declaredField.setAccessible(true);
            if (Collection.class.isAssignableFrom(fieldType)) {
                boolean isList = List.class.isAssignableFrom(fieldType);
                Type genericType = ClassUtils.getParamGenericType(declaredField.getGenericType());
                if (ClassUtils.shouldNew(genericType)) {
                    try {
                        Object fieldObj = newObj(Class.forName(genericType.getTypeName(), true, classLoader),
                                classLoader, ++deep);
                        declaredField.set(object, isList ? Lists.newArrayList(fieldObj) : Sets.newHashSet(fieldObj));
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        declaredField.set(object, isList ? Lists.newArrayList() : Sets.newHashSet());
                    }
                } else {
                    declaredField.set(object, isList ? Lists.newArrayList() : Sets.newHashSet());
                }
            } else {
                if (ClassUtils.shouldNew(fieldType)) {
                    try {
                        Object fieldObj = newObj(fieldType, classLoader, ++deep);
                        declaredField.set(object, fieldObj);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        return object;
    }

    /**
     * 返回已经注册的服务名称
     *
     * @param zk 指定的zk地址
     * @return 返回指定zk下面的所有已经注册的服务名称
     */
    @RequestMapping(value = "result/serviceNames", method = {RequestMethod.GET})
    @ResponseBody
    public WebApiRspDto<Set<Object>> getCreatedServiceName(@RequestParam(value = "zk") String zk) {
        Set<Object> serviceNameSet = cacheService.members(zk);
        return WebApiRspDto.success(serviceNameSet.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * 返回所有的接口
     *
     * @param interfaceKey 接口key group/interfaceName:version
     */
    @RequestMapping(value = "result/interface", method = {RequestMethod.GET})
    @ResponseBody
    public WebApiRspDto<InterfaceEntity> getInterfaces(@RequestParam("zk") String zk,
                                                       @RequestParam("serviceName") String serviceName,
                                                       @RequestParam("interfaceKey") String interfaceKey) {
        String serviceKey = BuildUtils.buildServiceKey(zk, serviceName);
        PostmanService service = InvokeContext.getService(serviceKey);
        if (service == null) {
            return WebApiRspDto.error("服务不存在,请先创建或刷新服务!");
        }
        InvokeContext.checkExistAndLoad(service);
        Integer type = redisRepository.mapGet(RedisKeys.CLUSTER_REDIS_KEY_TYPE, zk);
        RegisterCenterType registrationCenterType = RegisterCenterType.getByType(type);
        RegisterFactory registerFactory = getRegisterFactory(type);
        Register register = registerFactory.get(zk);
        Map<String, InterfaceMetaInfo> serviceMap;
        InterfaceEntity res = null;
        if (registrationCenterType == RegisterCenterType.ZK) {
            serviceMap = register.getAllService().get(serviceName);
            for (InterfaceEntity interfaceModel : service.getInterfaceModels()) {
                InterfaceMetaInfo metaItem = serviceMap.get(interfaceKey);
                //根据接口名称匹配接口对应的服务
                if (interfaceModel.getKey().equals(interfaceKey)) {
                    //用于实时同步应用的所有ip
                    interfaceModel.setServerIps(metaItem.getServerIps());
                    res = interfaceModel;
                    break;
                }
            }
        } else if (registrationCenterType == RegisterCenterType.EUREKA) {
            for (InterfaceEntity interfaceModel : service.getInterfaceModels()) {
                if (interfaceModel.getKey().equals(interfaceKey)) {
                    interfaceModel.setServerIps(new HashSet<>(register.getServiceInstances(serviceName)));
                    res = interfaceModel;
                }
            }
        } else {
            throw new RuntimeException("error:" + type);
        }
        LinkedHashSet<String> names = Objects.requireNonNull(res).getMethodNames().stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        res.setMethodNames(names);
        List<MethodEntity> methodEntities = res.getMethods().stream()
                .sorted(Comparator.comparing(MethodEntity::getName))
                .collect(Collectors.toList());
        res.setMethods(methodEntities);
        return WebApiRspDto.success(res);
    }

    /**
     * 根据服务名称,接口key,方法路径,获取参数模板
     */
    @RequestMapping(value = "result/interface/method/param", method = {RequestMethod.GET})
    @ResponseBody
    public WebApiRspDto<Map<String, Object>> getResultServiceMethod(@RequestParam("zk") String zk,
                                                                    @RequestParam("serviceName") String serviceName,
                                                                    @RequestParam("interfaceKey") String interfaceKey,
                                                                    @RequestParam("methodPath") String methodPath) {
        String serviceKey = BuildUtils.buildServiceKey(zk, serviceName);
        PostmanService service = InvokeContext.getService(serviceKey);
        if (service == null) {
            return WebApiRspDto.error("服务不存在,请先创建或刷新服务!");
        }
        InvokeContext.checkExistAndLoad(service);
        Map<String, Object> param = Maps.newLinkedHashMap();
        //重启之后在访问的时候重新load
        ApiJarClassLoader classLoader = JarLocalFileLoader.getAllClassLoader().get(serviceKey);
        if (classLoader == null) {
            return WebApiRspDto.error("加载类异常,classLoader为null");
        }
        for (InterfaceEntity serviceModel : service.getInterfaceModels()) {
            if (!serviceModel.getKey().equals(interfaceKey)) {
                continue;
            }
            for (MethodEntity methodModel : serviceModel.getMethods()) {
                if (!methodModel.getName().equals(methodPath)) {
                    continue;
                }
                for (Parameter parameter : methodModel.getMethod().getParameters()) {
                    Object value = newObj(parameter, classLoader);
                    param.put(parameter.getName(), JSONObject.toJSON(value));
                }
            }
        }
        return WebApiRspDto.success(param);
    }

    /**
     * 返回所有的接口
     *
     * @param serviceName 服务名称
     */
    @RequestMapping(value = "result/interfaceNames", method = {RequestMethod.GET})
    @ResponseBody
    public WebApiRspDto<Map<String, String>> getInterfaces(@RequestParam("zk") String zk, @RequestParam("serviceName") String serviceName) {
        Map<String, String> interfaceMap = getAllSimpleClassName(zk, serviceName);
        return WebApiRspDto.success(interfaceMap);

    }

}
