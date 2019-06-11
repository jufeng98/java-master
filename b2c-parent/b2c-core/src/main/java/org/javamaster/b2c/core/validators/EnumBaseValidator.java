package org.javamaster.b2c.core.validators;

import org.javamaster.b2c.core.annos.EnumCodeRange;
import org.javamaster.b2c.core.enums.EnumBase;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 校验实现了EnumBase接口的枚举对象的code是否满足要求
 *
 * @author yudong
 * @date 2019/6/10
 */
public class EnumBaseValidator implements ConstraintValidator<EnumCodeRange, EnumBase> {
    private long min;
    private long max;

    @Override
    public void initialize(EnumCodeRange constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(EnumBase value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.getCode() < min || value.getCode() > max) {
            return false;
        }
        return true;
    }
}
