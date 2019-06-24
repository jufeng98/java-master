package org.javamaster.b2c.classloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yudong
 * @date 2019/6/24
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.javamaster.b2c")
public class ClassLoaderApplication {

    public static void main(String[] args) {
        // 关闭devtools重启功能
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(ClassLoaderApplication.class, args);
    }

}
