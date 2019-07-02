package org.javamaster.b2c.bytecode.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * @author yudong
 * @date 2019/7/2
 */
public class ResourceUtils {
    public static final String CLASSPATH_URL_PREFIX = "classpath:";


    public static File getFile(String resourceLocation) {
        try {
            if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
                String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
                ClassLoader cl = ResourceUtils.class.getClassLoader();
                URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
                return new File(new URI(url.toString().replace(" ", "%20")).getSchemeSpecificPart());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
