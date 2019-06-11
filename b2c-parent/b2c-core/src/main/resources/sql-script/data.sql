-- 密码是123456
insert into sys_user values('1050106266','$2a$10$fyglVJ8Flczap1mIUiSMp.b5VFYEW8F3Fn72qrnpoDlase2Mkhq9O',true);
insert into sys_authority values('SA001','ROLE_ADMIN');
insert into sys_group values('SG001','系统管理员');
insert into sys_group_user values('SG001','1050106266');
insert into sys_group_authority values('SG001','SA001');
insert into microwebsite_exam values('BE000001','期末考试',1,1,false);
insert into microwebsite_exam_user values('BE000001','1050106266');