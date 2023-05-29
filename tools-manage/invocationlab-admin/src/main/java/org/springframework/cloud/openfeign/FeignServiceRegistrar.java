package org.springframework.cloud.openfeign;

import org.javamaster.invocationlab.admin.service.GAV;
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import lombok.SneakyThrows;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author yudong
 * @date 2022/11/12
 */
public class FeignServiceRegistrar {
    @SneakyThrows
    public static GenericApplicationContext register(GAV gav, ApiJarClassLoader apiJarClassLoader) {
        FeignClientsRegistrar registrar = new FeignClientsRegistrar();
        registrar.setResourceLoader(new ApiJarResourceLoader(apiJarClassLoader));
        registrar.setEnvironment(SpringUtils.getContext().getEnvironment());
        GenericApplicationContext context = new GenericApplicationContext();
        context.setParent(SpringUtils.getContext());
        context.setClassLoader(apiJarClassLoader);
        FeignContext feignContext = new FeignContext();
        feignContext.setApplicationContext(context);
        context.getBeanFactory().registerSingleton("feignContext", feignContext);
        FeignClientProperties feignClientProperties = new FeignClientProperties();
        feignClientProperties.setDefaultToProperties(true);
        feignClientProperties.setDefaultConfig("defaultConfig");
        FeignClientProperties.FeignClientConfiguration config = new FeignClientProperties.FeignClientConfiguration();
        config.setConnectTimeout(10000);
        config.setReadTimeout(10000);
        feignClientProperties.getConfig().put("defaultConfig", config);
        context.getBeanFactory().registerSingleton("feignClientProperties", feignClientProperties);
        context.getBeanFactory().registerSingleton("feignClient", new FeignClientDefault());
        context.getBeanFactory().registerSingleton("feignTargeter", new HystrixTargeter());
        registrar.registerBeanDefinitions(
                new MockAnnotationMetadata(FeignServiceRegistrar.class, gav.getGroupID()),
                context
        );
        context.refresh();
        return context;
    }

}
