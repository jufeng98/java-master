package org.javamaster.spring.swagger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author yudong
 * @date 2022/1/4
 */
@Slf4j
@SpringBootApplication
public class SwaggerApplication {

    private static ApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(SwaggerApplication.class, args);
        Environment environment = context.getEnvironment();
        log.info("swagger2 url: http://localhost:{}/doc.html#/", environment.getProperty("server.port"));
    }

    public static ApplicationContext getContext() {
        return context;
    }

}
