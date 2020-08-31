package org.javamaster.mybatis.generator.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author yudong
 * @date 2020/8/31
 */
public class PropertiesUtils {

    private static Properties properties;

    static {
        try {
            File propFile = ResourceUtils.getFile("classpath:generatorConfig.properties");
            properties = new Properties();
            properties.load(new FileInputStream(propFile));
            String projectPath = new File("").getAbsolutePath();
            properties.put("project.path", projectPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties getInstance() {
        return properties;
    }

    public static String getProp(String key) {
        return properties.getProperty(key);
    }

    public static String getProp(String key, String defaultValue) {
        String value = getProp(key);
        if (StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return value;
    }

    public static boolean getPropAsBoolean(String key, String defaultValue) {
        String value = getProp(key, defaultValue);
        return Boolean.parseBoolean(value);
    }

}
