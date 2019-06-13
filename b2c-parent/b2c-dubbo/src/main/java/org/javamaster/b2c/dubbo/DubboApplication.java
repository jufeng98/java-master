package org.javamaster.b2c.dubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yudong
 * @date 2019/6/13
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.javamaster.b2c")
public class DubboApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboApplication.class, args);
    }

}
