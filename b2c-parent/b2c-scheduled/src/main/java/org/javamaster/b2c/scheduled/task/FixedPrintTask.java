package org.javamaster.b2c.scheduled.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 * @date 2019/8/24
 */
@Component
public class FixedPrintTask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int i;

    @Scheduled(cron = "*/15 * * * * ?")
    public void execute() {
        logger.info("thread id:{},FixedPrintTask execute times:{}", Thread.currentThread().getId(), ++i);
    }

}
