package org.javamaster.invocationlab.admin.service.load.impl;

import org.javamaster.invocationlab.admin.config.BizException;
import org.javamaster.invocationlab.admin.consts.Constant;
import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.context.InvokeContext;
import org.javamaster.invocationlab.admin.service.creation.entity.InterfaceEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.MethodEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.ParamEntity;
import org.javamaster.invocationlab.admin.service.creation.entity.PostmanService;
import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam;
import org.javamaster.invocationlab.admin.service.load.Loader;
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.javamaster.invocationlab.admin.util.BuildUtils;
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 通过ApiJarClassLoader来实现api.jar的加载
 *
 * @author yudong
 */
public class JarLocalFileLoader implements Loader {
    private static final Logger logger = LoggerFactory.getLogger(JarLocalFileLoader.class);
    private static final Map<String, ApiJarClassLoader> LOADER_MAP = new ConcurrentHashMap<>();

    public static void loadInterfaceInfoAndInitLoader(PostmanService service) {
        try {
            doLoadInterfaceInfoAndInitLoader(service);
        } catch (Exception e) {
            logger.error("error", e);
            throw new BizException("缺少class,重新刷新服务即可解决!!!");
        }
    }

    public static String getJarLibPath(String serviceName, String version) {
        String apiJarPath = System.getProperty(Constant.USER_HOME);
        String versionDirName = version.replaceAll("\\.", "_");
        File dir = new File(apiJarPath + File.separator + versionDirName + "_" + serviceName);
        return dir.getAbsolutePath() + File.separator + "lib";
    }

    public static String getApiFilePath(String serviceName, GAV gav) {
        String jarPath = getJarLibPath(serviceName, gav.getVersion());
        return jarPath + File.separator + gav.getArtifactID() + ".jar";
    }

    public static Map<String, ApiJarClassLoader> getAllClassLoader() {
        return LOADER_MAP;
    }

    public static List<URL> getJarLibUrls(String jarPath) {
        List<File> jarLibFiles = getJarLibFiles(jarPath);

        return jarLibFiles.stream()
                .map(JarLocalFileLoader::getFileUrls)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<File> getJarLibFiles(String jarPath) {
        File baseFile = new File(jarPath);
        File[] files = baseFile.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files).collect(Collectors.toList());
    }

    private static URL getFileUrls(File jarFile) {
        URL urls = null;
        try {
            urls = jarFile.toURI().toURL();
        } catch (MalformedURLException e) {
            logger.warn(jarFile.getAbsolutePath() + "转换为url失败", e);
        }
        return urls;
    }

    public static ApiJarClassLoader initClassLoader(String serviceName, String version) {
        String jarLibPath = getJarLibPath(serviceName, version);
        List<URL> libUrlList = getJarLibUrls(jarLibPath);
        return new ApiJarClassLoader(libUrlList.toArray(new URL[0]));
    }

    /**
     * 通过ApiJarClassLoader加载所有的接口,同时解析接口里面的所有方法构建运行时类信息,添加到invokeContext里面
     */
    private static void doLoadInterfaceInfoAndInitLoader(PostmanService service) throws Exception {
        String serviceKey = BuildUtils.buildServiceKey(service.getCluster(), service.getServiceName());

        ApiJarClassLoader oldClzLoader = LOADER_MAP.get(serviceKey);
        if (oldClzLoader != null) {
            logger.info("销毁旧加载器:{}", oldClzLoader.getClass());
            oldClzLoader.close();
        }

        String oldGavVersion = ThreadLocalUtils.get(ThreadLocalUtils.OLD_GAV_VERSION);
        if (StringUtils.isNotBlank(oldGavVersion)) {
            String jarLibPath = getJarLibPath(service.getServiceName(), oldGavVersion);
            File parentFile = new File(jarLibPath).getParentFile();
            FileUtils.deleteDirectory(parentFile);
            ThreadLocalUtils.remove(ThreadLocalUtils.OLD_GAV_VERSION);
            logger.info("完成删除旧版本服务目录:{}", parentFile);
        }

        ApiJarClassLoader apiJarClassLoader = initClassLoader(service.getServiceName(), service.getGav().getVersion());
        LOADER_MAP.put(serviceKey, apiJarClassLoader);
        logger.info("初始化新加载器:{}", apiJarClassLoader.getClass());

        for (InterfaceEntity interfaceModel : service.getInterfaceModels()) {
            Set<String> methodNames = interfaceModel.getMethodNames();
            Class<?> clazz;
            try {
                clazz = apiJarClassLoader.loadClassWithResolve(interfaceModel.getInterfaceName());
            } catch (ClassNotFoundException e) {
                logger.error("找不到class,有可能是注册了dubbo服务,但是class文件却没放到api包里暴露出去:{}", e.getMessage());
                continue;
            }
            Method[] methods = clazz.getDeclaredMethods();
            //清空之前的内容，应用在重启的时候会重新load class,所以需要把原来的clear,再加进去一次，这个以后可以优化
            interfaceModel.getMethods().clear();
            for (Method method : methods) {
                //只加载应用注册到zk里面的方法
                if (!methodNames.contains(method.getName()) || !Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                MethodEntity methodModel = new MethodEntity();
                //设置运行时信息
                methodModel.setMethod(method);
                StringBuilder paramStr = new StringBuilder("(");
                List<RequestParam> requestParamList = new ArrayList<>();
                //如果没有参数，表示空参数，在调用的时候是空数组，没有问题
                for (Parameter parameter : method.getParameters()) {
                    ParamEntity paramModel = new ParamEntity();
                    paramModel.setName(parameter.getName());
                    paramModel.setType(parameter.getParameterizedType().getTypeName());
                    String wholeName = parameter.getParameterizedType().getTypeName();
                    String simpleName = wholeName.substring(wholeName.lastIndexOf(".") + 1);
                    paramStr.append(simpleName).append(",");
                    methodModel.getParams().add(paramModel);
                    RequestParam requestParam = new RequestParam();
                    requestParam.setParaName(parameter.getName());
                    requestParam.setTargetParaType(parameter.getType());
                    requestParamList.add(requestParam);
                }
                String allParamsName;
                if (paramStr.length() == 1) {
                    allParamsName = paramStr + ")";
                } else {
                    allParamsName = paramStr.substring(0, paramStr.length() - 1) + ")";
                }
                interfaceModel.getMethods().add(methodModel);
                String extendMethodName = method.getName() + allParamsName;
                methodModel.setName(extendMethodName);
                String methodKey = BuildUtils.buildMethodNameKey(service.getCluster(),
                        service.getServiceName(),
                        interfaceModel.getGroup(),
                        interfaceModel.getInterfaceName(),
                        interfaceModel.getVersion(),
                        methodModel.getName());
                InvokeContext.putMethod(methodKey, requestParamList);
            }
            //设置运行时信息,主要用于在页面请求的时候可以通过json序列化为json string的格式
            interfaceModel.setInterfaceClass(clazz);
        }
        logger.info("完成载入服务{}共{}个接口到InvokeContext", service.getServiceName(), service.getInterfaceModels().size());
        service.setLoadedToClassLoader(true);
    }
}
