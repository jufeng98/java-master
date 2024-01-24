package org.javamaster.invocationlab.admin;

import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.util.TimeZone;

/**
 * @author yudong
 */
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableAspectJAutoProxy
@EnableWebSocket
@EnableFeignClients({
        "org.javamaster.invocationlab.admin.feign"
})
@ServletComponentScan({
        "org.javamaster.invocationlab.admin.filter"
})
public class InvocationlabAdminApplication {
    private static final Logger logger = LoggerFactory.getLogger(InvocationlabAdminApplication.class);

    public static void main(String[] args) {
        logger.info("开始启动 MhWashInvocationlabAdminApplication");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        SpringApplication app = new SpringApplication(InvocationlabAdminApplication.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        String env = System.getenv("ENV");
        if (StringUtils.equals(env, "PRO")) {
            app.setAdditionalProfiles(env.toLowerCase());
            SpringUtils.setProEnv(true);
        } else if (StringUtils.equals(env, "DEV")) {
            SpringUtils.setDevEnv(true);
        }
        app.run(args);
        logger.info("MhWashInvocationlabAdminApplication 启动成功,当前环境:{}!", env);
    }
}
