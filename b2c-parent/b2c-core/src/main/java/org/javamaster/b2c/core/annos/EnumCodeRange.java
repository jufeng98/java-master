package org.javamaster.b2c.core.annos;


import org.javamaster.b2c.core.validators.EnumBaseValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 约束实现了EnumBase接口枚举的code的范围
 *
 * @author yudong
 * @date 2019/6/10
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumBaseValidator.class)
public @interface EnumCodeRange {
    String message() default "{org.hibernate.validator.constraints.Range.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long min() default 0L;

    long max() default 9223372036854775807L;
}
