package org.javamaster.invocationlab.admin.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 */
@Slf4j
public class FileUtils {

    @SneakyThrows
    public static String readStringFromUrl(URL url) {
        if (url == null) {
            return null;
        }
        URLConnection urlConnection = url.openConnection();
        urlConnection.setUseCaches(false);
        @Cleanup
        InputStream in = urlConnection.getInputStream();
        int len = in.available();
        byte[] data = new byte[len];
        int read = in.read(data, 0, len);
        log.info("read url:{} length:{}", url, read);
        return new String(data, StandardCharsets.UTF_8);
    }

}
