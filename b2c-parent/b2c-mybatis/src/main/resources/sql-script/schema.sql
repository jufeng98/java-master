drop table if exists `exams`;
CREATE TABLE `exams` (
  `id`              int(11)      NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `exams_code`      varchar(6)   NOT NULL
  COMMENT '考试编码,E+5位数字',
  `exams_name`      varchar(32)  NOT NULL
  COMMENT '考试名称',
  `exams_desc`      varchar(100) NOT NULL DEFAULT ''
  COMMENT '考试说明',
  `certs_id`        int(11)      NOT NULL DEFAULT 0
  COMMENT '关联证书表certs_id',
  `create_username` varchar(200) NOT NULL DEFAULT ''
  COMMENT '创建人名称',
  `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '考试表';

drop table if exists `menus`;
CREATE TABLE `menus` (
  `id`           int(11)      NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `parent_id`    int(11)      NOT NULL DEFAULT 0
  COMMENT '父id,0:顶级菜单;1:非顶级菜单',
  `has_sub_menu` tinyint      NOT NULL DEFAULT 0
  COMMENT '是否有子菜单,0:无;1:有',
  `name`         varchar(20)  NOT NULL
  COMMENT '名称',
  `path`         varchar(128) NOT NULL DEFAULT ''
  COMMENT 'path',
  `should_show`  tinyint      NOT NULL DEFAULT 1
  COMMENT '是否应显示,0:否;1:是',
  `icon`         varchar(30)  NOT NULL DEFAULT ''
  COMMENT '图标类名',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `op_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '最后操作时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '菜单表'



