-- Spring Security使用的表结构
drop table if exists sys_user;
CREATE TABLE sys_user (
  username varchar(20) primary key,
  password varchar(100),
  enabled tinyint
);
comment on table sys_user is '系统用户表';
comment on column sys_user.username is '用户名';
comment on column sys_user.password is '加密后密码';
comment on column sys_user.enabled is '用户状态,false不可用';
drop table if exists sys_authority;
CREATE TABLE sys_authority (
  authority_code varchar(5) primary key,
  authority_name varchar(10)
);
comment on table sys_authority is '系统用户表';
comment on column sys_authority.authority_code is '权限编码';
comment on column sys_authority.authority_name is '权限名称';
drop table if exists sys_group;
CREATE TABLE sys_group (
  group_code char(5) primary key,
  group_name varchar(10)
);
comment on table sys_group is '系统组表';
comment on column sys_group.group_code is '组编码';
comment on column sys_group.group_name is '组名称';
drop table if exists sys_group_user;
CREATE TABLE sys_group_user (
  group_code char(5),
  username   varchar(20)
);
comment on table sys_group_user is '系统组关联用户表';
comment on column sys_group_user.group_code is '关联系统组表组编码';
comment on column sys_group_user.username is '关联系统用户表用户名';
drop table if exists sys_group_authority;
CREATE TABLE sys_group_authority (
  group_code     char(5),
  authority_code varchar(12)
);
comment on table sys_group_authority is '系统组关联权限表';
comment on column sys_group_authority.group_code is '关联系统组表组编码';
comment on column sys_group_authority.authority_code is '关联系统权限表权限编码';
-- remember me表结构
drop table if exists sys_remember_me;
CREATE TABLE sys_remember_me (
  series    varchar(32) primary key,
  username  varchar(20),
  token     varchar(32),
  last_used timestamp
);
comment on table sys_remember_me is '系统记住登录用户表';
comment on column sys_remember_me.series is '序列号';
comment on column sys_remember_me.username is '关联用户表用户名';
comment on column sys_remember_me.token is 'token';
comment on column sys_remember_me.last_used is '最后使用时间';
-- 业务表结构
drop table if exists microwebsite_exam;
CREATE TABLE microwebsite_exam (
  exam_code   char(8),
  exam_name   varchar(20),
  exam_type   tinyint,
  exam_status tinyint,
  exam_op_date date,
  exam_op_username varchar(20),
  del_flag    tinyint
);
comment on table microwebsite_exam is '考试表';
comment on column microwebsite_exam.exam_code is '考试编码,ME开头';
comment on column microwebsite_exam.exam_name is '考试名称';
comment on column microwebsite_exam.exam_type is '考试类型,1:独立考试;2:关联课程';
comment on column microwebsite_exam.exam_status is '考试状态,1:未开始;2:待考试;3:考试中;4:已完成';
comment on column microwebsite_exam.exam_op_date is '考试最后操作日期';
comment on column microwebsite_exam.exam_op_username is '考试最后操作人';
comment on column microwebsite_exam.del_flag is '删除标志,true:已删除';
drop table if exists microwebsite_exam_user;
CREATE TABLE microwebsite_exam_user (
  exam_code char(8),
  username  varchar(20)
);
comment on table microwebsite_exam_user is '考试关联用户表';
comment on column microwebsite_exam_user.exam_code is '关联考试表考试编码';
comment on column microwebsite_exam_user.username  is '关联用户表用户名';