package org.javamaster.b2c.mybatis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author yudong
 * @date 2019/9/1
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "org.javamaster.b2c")
public class MybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisApplication.class, args);
    }

}
