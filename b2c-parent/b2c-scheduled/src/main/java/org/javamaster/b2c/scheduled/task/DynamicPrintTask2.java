package org.javamaster.b2c.scheduled.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 * @date 2019/5/10
 */
@Component
public class DynamicPrintTask2 implements ScheduledOfTask {

    Logger logger = LoggerFactory.getLogger(getClass());

    private int i;

    @Override
    public void execute() {
        logger.info("thread id:{},DynamicPrintTask2 execute times:{}", Thread.currentThread().getId(), ++i);
    }

}
