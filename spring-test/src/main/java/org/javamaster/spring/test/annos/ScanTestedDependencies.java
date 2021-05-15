package org.javamaster.spring.test.annos;

import java.lang.annotation.*;

/**
 * 扫描指定的类和接口的所有子类的依赖.通过查看类字段是否带有Autowired,将其class加入到ContextConfiguration的classes里.
 * 对于不带有Autowired的特殊依赖字段,使用additionalInterfaces指定.
 * <br/>
 * 注意:只会扫描当前模块(即target目录下的class),不会去扫描jar包(为了节省时间),若依赖位于其他jar包,则需在ContextConfiguration注解中显式指明
 *
 * @author yudong
 * @date 2021/5/15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ScanTestedDependencies {
    Class<?> value();

    Class<?>[] additionalInterfaces() default {};
}
