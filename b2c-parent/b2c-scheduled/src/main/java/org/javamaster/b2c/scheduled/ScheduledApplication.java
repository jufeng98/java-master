package org.javamaster.b2c.scheduled;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author yudong
 * @date 2019/8/24
 */
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = "org.javamaster.b2c")
@EnableTransactionManagement
@SpringBootApplication
public class ScheduledApplication {
    private static Logger logger = LoggerFactory.getLogger(ScheduledApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ScheduledApplication.class, args);
        logger.info("定时任务页面管理地址:{}", "http://localhost:8089/scheduled/task/taskList");
    }

}
