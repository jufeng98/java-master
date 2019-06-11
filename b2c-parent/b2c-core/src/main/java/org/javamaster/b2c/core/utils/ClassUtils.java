package org.javamaster.b2c.core.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2019/6/10
 */
public class ClassUtils {

    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private static MetadataReaderFactory metaReader = new CachingMetadataReaderFactory();

    /**
     * 获取包下所有的Class
     *
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getAllClassesFromPackage(String packageName) {
        String dir = packageName.replace(".", "/");
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources("classpath*:" + dir + "/**/*.class");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(resources).map(resource -> {
            try {
                return Class.forName(metaReader.getMetadataReader(resource).getClassMetadata().getClassName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet());
    }


}
