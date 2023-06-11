package org.javamaster.invocationlab.admin.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * @author yudong
 * @date 2023/6/7
 */
public class ResponseUtils {

    @SneakyThrows
    public static void jsonGzipResponse(HttpServletResponse response, String jsonStr) {
        response.setHeader("Content-Encoding", "gzip");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        @Cleanup
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        @Cleanup
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        gzipOutputStream.finish();
        @Cleanup
        ServletOutputStream realOutPutStream = response.getOutputStream();
        byteArrayOutputStream.writeTo(realOutPutStream);
        realOutPutStream.flush();
    }

}
