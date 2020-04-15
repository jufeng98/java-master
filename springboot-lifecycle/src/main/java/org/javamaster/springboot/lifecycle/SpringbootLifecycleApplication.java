package org.javamaster.springboot.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.management.ManagementFactory;

/**
 * @author yudong
 * @date 2020/4/14
 */
@SpringBootApplication // 开启组件扫描和自动配置
public class SpringbootLifecycleApplication {

    public static void main(String[] args) {
        // 负责启动引导应用程序
        SpringApplication.run(SpringbootLifecycleApplication.class, args);
        System.out.println("PID:" + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        System.out.println("http://localhost:8080/portal/index");
    }

}
