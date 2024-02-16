package org.springframework.cloud.openfeign

import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.springframework.cloud.openfeign.FeignClientProperties.FeignClientConfiguration
import org.springframework.context.support.GenericApplicationContext

/**
 * @author yudong
 * @date 2022/11/12
 */
object FeignServiceRegistrar {

    fun register(feignServicePackages: List<String>, apiJarClassLoader: ApiJarClassLoader): GenericApplicationContext {
        val registrar = FeignClientsRegistrar()
        registrar.setResourceLoader(ApiJarResourceLoader(apiJarClassLoader))
        registrar.setEnvironment(SpringUtils.context.environment)

        val context = GenericApplicationContext()
        context.parent = SpringUtils.context
        context.classLoader = apiJarClassLoader

        val feignContext = FeignClientFactory()
        feignContext.setApplicationContext(context)
        context.beanFactory.registerSingleton("feignContext", feignContext)

        val feignClientProperties = FeignClientProperties()
        feignClientProperties.isDefaultToProperties = true
        feignClientProperties.defaultConfig = "defaultConfig"

        val config = FeignClientConfiguration()
        config.connectTimeout = 10000
        config.readTimeout = 10000
        feignClientProperties.config["defaultConfig"] = config

        context.beanFactory.registerSingleton("feignClientProperties", feignClientProperties)
        context.beanFactory.registerSingleton("feignClient", FeignClientDefault())
        context.beanFactory.registerSingleton("feignTargeter", DefaultTargeter())

        val metadata = MockAnnotationMetadata(FeignServiceRegistrar::class.java, feignServicePackages)
        registrar.registerBeanDefinitions(metadata, context)
        context.refresh()

        return context
    }

}
