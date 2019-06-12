-- Spring Security使用的表结构
drop table if exists sys_user;
CREATE TABLE sys_user (
  username varchar(20) primary key,
  password varchar(100),
  enabled  boolean
);
drop table if exists sys_authority;
CREATE TABLE sys_authority (
  authority_code varchar(5) primary key,
  authority_name varchar(10)
);
drop table if exists sys_group;
CREATE TABLE sys_group (
  group_code char(5) primary key,
  group_name varchar(10)
);
drop table if exists sys_group_user;
CREATE TABLE sys_group_user (
  group_code char(5),
  username   varchar(20)
);
drop table if exists sys_group_authority;
CREATE TABLE sys_group_authority (
  group_code     char(5),
  authority_code varchar(12)
);
-- remember me表结构
drop table if exists sys_remember_me;
CREATE TABLE sys_remember_me (
  series    varchar(32) primary key,
  username  varchar(20),
  token     varchar(32),
  last_used timestamp
);
-- 业务表结构
drop table if exists microwebsite_exam;
CREATE TABLE microwebsite_exam (
  exam_code   char(8),
  exam_name   varchar(20),
  exam_type   tinyint,
  exam_status tinyint,
  del_flag    boolean
);
drop table if exists microwebsite_exam_user;
CREATE TABLE microwebsite_exam_user (
  exam_code char(8),
  username  varchar(20)
);