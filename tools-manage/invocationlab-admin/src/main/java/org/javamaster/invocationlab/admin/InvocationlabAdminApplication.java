package org.javamaster.invocationlab.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.TimeZone;

/**
 * @author yudong
 */
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableAspectJAutoProxy
public class InvocationlabAdminApplication {
    private static final Logger logger = LoggerFactory.getLogger(InvocationlabAdminApplication.class);

    public static void main(String[] args) {
        logger.info("开始启动 InvocationlabAdminApplication");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        SpringApplication app = new SpringApplication(InvocationlabAdminApplication.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
        logger.info("InvocationlabAdminApplication 启动成功!");
    }
}