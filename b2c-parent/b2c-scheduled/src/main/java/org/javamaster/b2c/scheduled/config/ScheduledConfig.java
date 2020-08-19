package org.javamaster.b2c.scheduled.config;

import org.javamaster.b2c.scheduled.entity.SpringScheduledCron;
import org.javamaster.b2c.scheduled.respsitory.SpringScheduledCronRepository;
import org.javamaster.b2c.scheduled.task.ScheduledOfTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.Assert;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author yudong
 * @date 2019/5/10
 */
@Configuration
public class ScheduledConfig implements SchedulingConfigurer {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private SpringScheduledCronRepository cronRepository;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        for (SpringScheduledCron springScheduledCron : cronRepository.findAll()) {
            Class<?> clazz;
            Object task;
            try {
                clazz = Class.forName(springScheduledCron.getCronKey());
                task = context.getBean(clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("spring_scheduled_cron表数据" + springScheduledCron.getCronKey() + "有误", e);
            } catch (BeansException e) {
                throw new IllegalArgumentException(springScheduledCron.getCronKey() + "未纳入到spring管理", e);
            }
            Assert.isAssignable(ScheduledOfTask.class, task.getClass(), "定时任务类必须实现ScheduledOfTask接口");
            // 可以通过改变数据库数据进而实现动态改变执行周期
            taskRegistrar.addTriggerTask(((Runnable) task),
                    triggerContext -> {
                        String cronExpression = cronRepository.findByCronKey(springScheduledCron.getCronKey()).getCronExpression();
                        return new CronTrigger(cronExpression).nextExecutionTime(triggerContext);
                    }
            );
        }

    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

}
