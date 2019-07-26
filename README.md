# java-master
Java高手之路

b2c-parent/b2c-core:
项目框架：Spring Boot + Spring Security + Spring AOP + Mybatis + Hibernate Validation API + Jackson + Redisson(高性能分布式锁)
项目管理工具：maven
项目使用内嵌的H2数据库，表结构定义在resources/sql-script/schema.sql，表数据定义在resources/sql-script/data.sql，所以
运行项目时不需要本地先安装数据库软件。Redisson使用了redis, 因此配置文件需要正确填写redis服务器地址
可以通过运行ExamControllerTest或UserControllerTest测试类查看项目运行效果或者启动项目，通过core-rest-api.http文件去调用接口
查看运行效果（仅适用IDEA），非IDEA等可使用postman或其它工具来调用接口

b2c-parent/b2c-dubbo:
写有一个示例Dubbo服务UserDubboService

b2c-parent/b2c-test
测试包:Java 8示例测试代码；Dubbo示例测试代码；Jackson示例测试代码,JSR 303(Hibernate Validator)示例测试代码

b2c-parent/b2c-classloader:
devtools依赖导致的序列化和反序列化问题示例代码

b2c-parent/b2c-bytecode:
读取解析class文件的示例代码

handypoi-excel
一个简洁方便读写excel的工具包,测试包里面有两个示例Test类,可通过查看和运行Test类查看具体用法

mocklombok-javac
一个jsr269(插入式注解处理器)的示例代码,展示了在编译时如何根据注解生成方法以及仿照findbugs等类编写规范检查