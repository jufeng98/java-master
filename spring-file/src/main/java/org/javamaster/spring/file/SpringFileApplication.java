package org.javamaster.spring.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yudong
 * @date 2021/2/8
 */
@SpringBootApplication
public class SpringFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringFileApplication.class, args);
        System.out.println("http://localhost:8896/webUploader.html");

    }

}
