drop table if exists `spring_scheduled_cron`;
create table `spring_scheduled_cron` (
  `cron_id`         int primary key           auto_increment
  comment '主键id',
  `cron_key`        varchar(128) not null unique
  comment '定时任务完整类名',
  `cron_expression` varchar(20)  not null
  comment 'cron表达式',
  `task_explain`    varchar(50)  not null     default ''
  comment '任务描述',
  `status`          tinyint      not null     default 1
  comment '状态,1:正常;2:停用'
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '定时任务表';

insert into `spring_scheduled_cron`
values (1, 'org.javamaster.b2c.scheduled.task.DynamicPrintTask', '*/5 * * * * ?', '定时任务描述', 1);
insert into `spring_scheduled_cron`
values (2, 'org.javamaster.b2c.scheduled.task.DynamicPrintTask1', '*/5 * * * * ?', '定时任务描述1', 1);
insert into `spring_scheduled_cron`
values (3, 'org.javamaster.b2c.scheduled.task.DynamicPrintTask2', '*/5 * * * * ?', '定时任务描述2', 1);