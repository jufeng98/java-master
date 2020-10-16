#!/bin/bash
# arthas在线教程文档
https://arthas.aliyun.com/doc/

#arthas IDEA插件
https://www.yuque.com/docs/share/fa77c7b4-c016-4de6-9fa3-58ef25a97948

# 在线安装
curl -O https://arthas.aliyun.com/arthas-boot.jar
java -jar arthas-boot.jar
# 如果下载速度比较慢，可以使用aliyun的镜像
java -jar arthas-boot.jar --repo-mirror aliyun --use-http

# 调用类的静态方法并得到方法返回结果
ognl '@java.lang.System@out.println("hello world")'
ognl '@java.lang.Math@random()'
ognl "@java.lang.Thread@currentThread()"
ognl "@java.lang.Thread@currentThread().getContextClassLoader()"

# 获取类的静态字段
ognl '@java.lang.System@out'
getstatic java.lang.System out

#执行多行表达式，赋值给临时变量，返回一个List
ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'

# 获取加载该类的classloader的hash码(SpringBoot项目有自己的类加载器)
sc -d org.javamaster.b2c.core.CoreApplication

# 指定使用的classloader的hash码,可调用被spring管理的任意bean的任意方法(应用里定义静态的ApplicationContext对象)
ognl -c 20ad9418 "#req1=new org.javamaster.b2c.core.model.vo.GetExamListReqVo(),
#req1.examType=@org.javamaster.b2c.core.enums.ExamTypeEnum@EXAM_INDEPENDENT,
#req2=new org.javamaster.b2c.core.model.AuthUser(),
#req2.username='1050106266',
#res1=@org.javamaster.b2c.core.utils.SpringUtils@applicationContext.getBean('IExamServiceImpl').getExamList(#req1,#req2),
#res2=new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(#res1),
{#res2}"

# 若应用没有暴露ApplicationContext,可以用此命令记录获取ApplicationContext
tt -t -n 1 org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter invokeHandlerMethod

# 拿到ApplicationContext
tt -i 1000 -w "target.getApplicationContext()"

# 拿到ServletContext
tt -i 1000 -w "target.getServletContext().context"

tt -i 1000 -w "#req1=new org.javamaster.b2c.core.model.vo.GetExamListReqVo(),
{#req1}"

# 若应用引入了dubbo,则可以这样拿到ApplicationContext
ognl -c 20ad9418 "#context=@com.alibaba.dubbo.config.spring.extension.SpringExtensionFactory@contexts.iterator.next,
{#context}"

# 查看占用CPU的线程
thread -n 3

# 查看JVM已加载的类信息
sc org.javamaster.b2c.core*

# 打印类的详细信息
sc -d -f org.javamaster.b2c.core.service.impl.IExamServiceImpl

# 查看已加载类的方法信息
sm org.javamaster.b2c.core.service.impl.IExamServiceImpl

# 查看类方法的详细信息
sm -d org.javamaster.b2c.core.service.impl.IExamServiceImpl

# 反编译class文件
jad org.javamaster.b2c.core.service.impl.IExamServiceImpl

# 查看类加载器
classloader -l

# 使用ClassLoader去加载类
classloader -c 439f5b3d --load /tmp/IExamServiceImpl.class

# 本地编译好改动的class文件,上传到服务器重新加载(局限:不允许新增加field/method)
# 和jad/watch/trace/monitor/tt等命令会冲突，执行完redefine之后，如果再执行前面
# 提到的命令，则会把redefine的字节码重置
redefine /tmp/IExamServiceImpl.class

# 方法执行监控
monitor -c 5 org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList

# 方法执行堆栈耗时
trace -E org.javamaster.b2c.core.controller.ExamController | org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList

# 观察方法调用前入参
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params}" -x 3 -b
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params[0].examType,params[1].username}" -x 3 -b
# ognl方式投影
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "params[1].{#this.username}"
# ognl方式过滤
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "params[1].{? #this.username.equals('1050106266') }" -x -2
# 指定过滤条件
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params}" "params.length==2" -x 3 -b
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params}" "params[1].username.equals('1050106266')" -x 3 -b
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params}" "params.length==2 && params[1].username instanceof String" -x 3 -b
# 观察方法调用后入参
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params}" -x 3 -s

# 观察方法调用后返回值
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{returnObj}" -x 2 -s

# 同时观察方法调用前后的入参和返回值
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{params,returnObj}" -x 2 -b -s

# 查看所有参数
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{loader,clazz,method,target,params,returnObj,throwExp,isBefore,isThrow,isReturn}" -x 1 -b -s

# 观察方法调用前对象的属性值
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{target}" -x 2 -b
# 观察方法调用前对象的具体某个属性值
watch org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "{target.examMapper.h}" -x 2 -b

# 输出当前方法的调用堆栈
stack org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList
# 按条件过滤
stack org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList "params[1].username.equals('1050106266')"

# 记录下指定方法每次调用的入参和返回信息
tt -t org.javamaster.b2c.core.service.impl.IExamServiceImpl getExamList
# 检索调用记录
tt -i 1000
# tt 命令由于保存了当时调用的所有现场信息，所以可重新触发方法调用
tt -i 1000 -p
