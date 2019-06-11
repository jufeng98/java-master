package org.javamaster.b2c.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author yudong
 * @date 2019/6/10
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackages = "org.javamaster.b2c")
public class CoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

}
