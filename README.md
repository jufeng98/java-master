# java-master
Java高手之路

b2c-core:
项目框架：Spring Boot + Spring Security + Spring AOP + Mybatis + Hibernate Validation API + Jackson
项目管理工具：maven
项目使用内嵌的H2数据库，表结构定义在resources/sql-script/schema.sql，表数据定义在resources/sql-script/data.sql，所以
运行项目时不需要本地先安装数据库软件。
可以通过运行ExamControllerTest测试类查看项目运行效果或者启动项目，通过core-rest-api.http文件去调用接口查看运行效果（仅适用IDEA）
非IDEA等可使用postman或其它工具来调用接口

b2c-dubbo:
写有一个示例Dubbo服务UserDubboService

b2c-test:
Java 8示例代码位于测试包下
Dubbo相关简易测试类DubboUtils和DubboTest