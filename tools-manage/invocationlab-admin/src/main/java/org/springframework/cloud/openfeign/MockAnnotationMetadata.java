package org.springframework.cloud.openfeign;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.util.List;
import java.util.Map;

/**
 * @author yudong
 * @date 2022/11/12
 */
public class MockAnnotationMetadata extends StandardAnnotationMetadata {

    private final List<String> feignServicePackages;

    public MockAnnotationMetadata(Class<?> introspectedClass, List<String> feignServicePackages) {
        super(introspectedClass);
        this.feignServicePackages = feignServicePackages;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(@NotNull String annotationName) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("value", feignServicePackages.toArray(new String[0]));
        map.put("basePackages", new String[0]);
        map.put("basePackageClasses", new Class[0]);
        map.put("defaultConfiguration", new Class[0]);
        map.put("clients", new Class[0]);
        return map;
    }
}
