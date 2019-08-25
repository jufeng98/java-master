package org.javamaster.b2c.scheduled.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author yudong
 * @date 2019/5/10
 */
@Entity(name = "spring_scheduled_cron")
public class SpringScheduledCron {
    @Id
    @Column(name = "cron_id")
    private Integer cronId;
    @Column(name = "cron_key")
    private String cronKey;
    @Column(name = "cron_expression")
    private String cronExpression;
    @Column(name = "task_explain")
    private String taskExplain;
    private Integer status;

    @Override
    public String toString() {
        return "SpringScheduledCron{" +
                "cronId=" + cronId +
                ", cronKey='" + cronKey + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", taskExplain='" + taskExplain + '\'' +
                ", status=" + status +
                '}';
    }

    public Integer getCronId() {
        return cronId;
    }

    public void setCronId(Integer cronId) {
        this.cronId = cronId;
    }

    public String getCronKey() {
        return cronKey;
    }

    public void setCronKey(String cronKey) {
        this.cronKey = cronKey;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getTaskExplain() {
        return taskExplain;
    }

    public void setTaskExplain(String taskExplain) {
        this.taskExplain = taskExplain;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
