package org.javamaster.b2c.test.validation;


import org.javamaster.b2c.test.model.validation.Person;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author yudong
 * @date 2021/4/27
 */
public class CheckPersonValidator implements ConstraintValidator<CheckPerson, Person> {
    private String name;
    private String city;

    @Override
    public void initialize(CheckPerson constraintAnnotation) {
        this.name = constraintAnnotation.name();
        this.city = constraintAnnotation.city();
    }

    @Override
    public boolean isValid(Person value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (!value.getName().equals(name)) {
            return true;
        }
        if (value.getAddress() == null) {
            return false;
        }
        return value.getAddress().getCity().equals(city);
    }

}
