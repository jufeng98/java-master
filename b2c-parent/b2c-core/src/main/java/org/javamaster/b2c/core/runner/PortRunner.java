package org.javamaster.b2c.core.runner;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

/**
 * 记录启动端口到文件
 *
 * @author yudong
 * @date 2020/9/20
 */
@Component
public class PortRunner implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    private ServletWebServerApplicationContext server;

    @Override
    public void run(String... args) {
        try {
            if (server == null) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            int port = server.getWebServer().getPort();
            String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            stringBuilder.append("# ").append(time).append("\r\n");
            stringBuilder.append("port=").append(port);
            File path = new File("tmp");
            if (!path.exists()) {
                Files.createDirectories(path.toPath());
            }
            File file = new File(path, "port.properties");
            Files.write(file.toPath(), stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
            log.info("tomcat port is:{},write to file:{}", port, file.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
