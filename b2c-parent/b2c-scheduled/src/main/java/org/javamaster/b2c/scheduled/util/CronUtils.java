package org.javamaster.b2c.scheduled.util;

import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author yudong
 * @date 2019/5/10
 */
public class CronUtils {
    private static Logger logger = LoggerFactory.getLogger(CronUtils.class);

    public static boolean isValidExpression(final String cronExpression) {
        CronTriggerImpl trigger = new CronTriggerImpl();
        try {
            trigger.setCronExpression(cronExpression);
            Date date = trigger.computeFirstFireTime(null);
            return date != null && date.after(new Date());
        } catch (Exception e) {
            logger.error("invalid expression:{},error msg:{}", cronExpression, e.getMessage());
        }
        return false;
    }

}
