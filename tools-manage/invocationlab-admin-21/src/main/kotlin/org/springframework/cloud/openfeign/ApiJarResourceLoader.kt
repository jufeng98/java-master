package org.springframework.cloud.openfeign

import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

/**
 * @author yudong
 * @date 2022/11/9
 */
class ApiJarResourceLoader(private val apiJarClassLoader: ApiJarClassLoader) : ResourceLoader {
    override fun getResource(location: String): Resource {
        throw UnsupportedOperationException()
    }

    override fun getClassLoader(): ClassLoader {
        return apiJarClassLoader
    }
}
