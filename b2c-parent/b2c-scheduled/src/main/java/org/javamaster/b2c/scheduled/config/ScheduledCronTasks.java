package org.javamaster.b2c.scheduled.config;

import org.javamaster.b2c.scheduled.entity.SpringScheduledCron;
import org.javamaster.b2c.scheduled.respsitory.SpringScheduledCronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2019/5/10
 */
@Component
public class ScheduledCronTasks {

    @Autowired
    private SpringScheduledCronRepository cronRepository;

    private List<SpringScheduledCron> cronList;

    @PostConstruct
    private void init() {
        List<SpringScheduledCron> springScheduledCrons = cronRepository.findAll();
        this.cronList = springScheduledCrons;
    }

    public SpringScheduledCron findByCronKey(String cronKey) {
        List<SpringScheduledCron> list = cronList.stream()
                .filter(cron -> cronKey.equals(cron.getCronKey())).collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new IllegalArgumentException("失败,cronKey有误:" + cronKey);
        }
        return list.get(0);
    }

    public List<SpringScheduledCron> getCronList() {
        return cronList;
    }

}
