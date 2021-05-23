package org.javamaster.spring.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author yudong
 * @date 2021/4/26
 */
@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
public class SpringAopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAopApplication.class, args);
        log.info("http://localhost:8899");
    }

}
