drop table if exists `spring_scheduled_cron`;
create table `spring_scheduled_cron` (
  `cron_id`         int auto_increment primary key,
  `cron_key`        varchar(128) not null unique,
  `cron_expression` varchar(20)  not null,
  `task_explain`    varchar(50),
  `status`          tinyint comment '1:正常;2:停用'
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