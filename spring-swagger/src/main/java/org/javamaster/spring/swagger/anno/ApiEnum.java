package org.javamaster.spring.swagger.anno;

import org.javamaster.spring.swagger.enums.EnumBase;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yudong
 * @date 2022/1/4
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiEnum {
    Class<? extends Enum<? extends EnumBase>> value();
}
