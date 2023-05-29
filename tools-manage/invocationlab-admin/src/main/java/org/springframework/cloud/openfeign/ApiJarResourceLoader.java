package org.springframework.cloud.openfeign;

import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author yudong
 * @date 2022/11/9
 */
@SuppressWarnings("NullableProblems")
public class ApiJarResourceLoader implements ResourceLoader {
    private final ApiJarClassLoader apiJarClassLoader;

    public ApiJarResourceLoader(ApiJarClassLoader apiJarClassLoader) {
        this.apiJarClassLoader = apiJarClassLoader;
    }

    @Override
    public Resource getResource(String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        return apiJarClassLoader;
    }
}
