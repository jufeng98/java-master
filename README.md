# java-master Java高手之路
## b2c-parent/b2c-core
* 项目框架：Spring Boot + Spring Security + Spring AOP + Mybatis + Hibernate Validation API + Jackson + Redisson(高性能分布式锁)
* 项目管理工具：maven
* 项目使用内嵌的H2数据库，表结构定义在resources/sql-script/schema.sql，表数据定义在resources/sql-script/data.sql，所以运行项目
  时不需要本地先安装数据库软件。Redisson使用了redis, 因此配置文件需要正确填写redis服务器地址.
* 可以通过运行ExamControllerTest或UserControllerTest测试类查看项目运行效果
* 或者启动项目,通过test/resources/core-rest-api.http文件去调用接口查看运行效果（仅适用IDEA），非IDEA等可使用postman或其它工具来调用接口
## b2c-parent/b2c-dubbo
写有一个示例Dubbo服务UserDubboService
## b2c-parent/b2c-test(普通jar模块,非微服务)
test/java目录内有:
* Java 8示例测试代码；
* Dubbo示例测试代码；
* Jackson示例测试代码;
* JSR 303(数据校验)示例测试代码
* 多线程示例代码
## b2c-parent/b2c-classloader
springboot devtools(开发者工具)依赖导致的序列化和反序列化问题示例代码
## b2c-parent/b2c-bytecode(普通jar模块,可被引用,非微服务)
读取解析class文件的示例代码
## handypoi-excel(普通jar模块,可被引用,非微服务)
一个简洁方便的可通过注解来读取excel文件以及生成excel文件的工具类库,简化Apache poi类库繁琐的操作.test/java目录内有三个示例Test类,
可通过查看和运行Test类了解具体用法
## mocklombok-javac(普通jar模块,可被引用,非微服务)
一个jsr269(插入式注解处理器)的示例代码,展示了在编译时如何根据注解生成方法以及仿照findbugs等实现类编写规范检查
## b2c-parent/b2c-scheduled
展示如何优雅实现定时任务
## mybatis-generator
根据数据库表结构生成相应的mybatis Java实体类,mapper类和mapper类关联的xml文件,从此告别手工操作,还展示了如何定制mybatis generator
的行为,使得生成的文件符合我们自己的需求
## b2c-parent/b2c-mybatis
展示mybatis-generator生成的相关mybatis实体类,mapper等文件,其中表结构文件schema.sql放在main/resources/sql-script目录内
## spring-lifecycle
用于研究spring的生命周期以及bean的生命周期
## springboot-lifecycle
用于研究springboot的生命周期以及bean的生命周期
## spring-transactional
spring @Transactional注解实现原理解析
## java-agent
阿里arthas的常见用法及其原理解析,并根据其实现原理写了示例代码,模拟其watch命令的功能