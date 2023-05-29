package org.springframework.cloud.openfeign;

import com.google.common.collect.Maps;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.util.Map;

/**
 * @author yudong
 * @date 2022/11/12
 */
public class MockAnnotationMetadata extends StandardAnnotationMetadata {
    private final String groupID;

    public MockAnnotationMetadata(Class<?> introspectedClass, String groupID) {
        super(introspectedClass);
        this.groupID = groupID;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("value", new String[]{groupID});
        map.put("basePackages", new String[0]);
        map.put("basePackageClasses", new Class[0]);
        map.put("defaultConfiguration", new Class[0]);
        map.put("clients", new Class[0]);
        return map;
    }
}
