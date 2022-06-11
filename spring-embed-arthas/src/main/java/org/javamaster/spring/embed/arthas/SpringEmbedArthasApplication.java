package org.javamaster.spring.embed.arthas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yudong
 * @date 2022/6/4
 */
@SpringBootApplication
public class SpringEmbedArthasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringEmbedArthasApplication.class, args);
        // 使用spring-boot-maven-plugin插件的spring-boot:run目标来启动项目
        System.out.println("http://localhost:8898/arthas-test.html");
    }

}
