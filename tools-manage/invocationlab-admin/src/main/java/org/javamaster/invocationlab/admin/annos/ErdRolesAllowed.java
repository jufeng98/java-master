package org.javamaster.invocationlab.admin.annos;

import org.javamaster.invocationlab.admin.enums.RoleEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author yudong
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD})
public @interface ErdRolesAllowed {
    RoleEnum[] value();

    String msg() default "无权操作";
}
