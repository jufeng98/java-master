package org.javamaster.b2c.scheduled.task;

import org.javamaster.b2c.scheduled.config.ScheduledCronTasks;
import org.javamaster.b2c.scheduled.entity.SpringScheduledCron;
import org.javamaster.b2c.scheduled.enums.StatusEnum;
import org.javamaster.b2c.scheduled.util.SpringUtils;

/**
 * @author yudong
 * @date 2019/5/11
 */
public interface ScheduledOfTask extends Runnable {

    /**
     * 定时任务方法
     */
    void execute();

    /**
     * 实现控制定时任务启用或禁用的功能
     */
    @Override
    default void run() {
        ScheduledCronTasks properties = SpringUtils.getBean(ScheduledCronTasks.class);
        SpringScheduledCron scheduledCron = properties.findByCronKey(this.getClass().getName());
        if (scheduledCron.getStatus().equals(StatusEnum.DISABLED.getCode())) {
            return;
        }
        execute();
    }
}
