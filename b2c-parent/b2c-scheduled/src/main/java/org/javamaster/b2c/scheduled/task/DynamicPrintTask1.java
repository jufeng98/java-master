package org.javamaster.b2c.scheduled.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yudong
 * @date 2019/5/10
 */
@Component
public class DynamicPrintTask1 implements ScheduledOfTask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int i;

    @Override
    public void execute() {
        logger.info("thread id:{},DynamicPrintTask1 execute times:{}", Thread.currentThread().getId(), ++i);
    }

}
