package org.javamaster.spring.transactional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author yudong
 * @date 2020/5/15
 */
@SpringBootApplication
@EnableTransactionManagement // 这行注解其实可以不需要,在TransactionAutoConfiguration自动配置类里已经带有此注解
public class SpringTransactionalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTransactionalApplication.class, args);
    }

}
