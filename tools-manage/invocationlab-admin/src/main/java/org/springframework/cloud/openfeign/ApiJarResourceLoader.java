package org.springframework.cloud.openfeign;

import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author yudong
 * @date 2022/11/9
 */
public class ApiJarResourceLoader implements ResourceLoader {
    private final ApiJarClassLoader apiJarClassLoader;

    public ApiJarResourceLoader(ApiJarClassLoader apiJarClassLoader) {
        this.apiJarClassLoader = apiJarClassLoader;
    }

    @Override
    public @NotNull Resource getResource(@NotNull String location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        return apiJarClassLoader;
    }
}
