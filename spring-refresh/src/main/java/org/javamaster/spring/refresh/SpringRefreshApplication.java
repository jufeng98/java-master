package org.javamaster.spring.refresh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author yudong
 * @date 2022/9/9
 */
@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SpringRefreshApplication {


    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SpringRefreshApplication.class, args);
        Environment environment = context.getEnvironment();
        log.info("url: http://localhost:{}/", environment.getProperty("server.port"));
    }

}
