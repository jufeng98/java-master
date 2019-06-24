package org.javamaster.b2c.test.model.validation;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.javamaster.b2c.test.validation.DriverChecks;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author yudong
 * @date 2019/6/17
 */
public class Driver extends Person {
    @NotNull
    @Min(value = 18, message = "必须年满18岁", groups = DriverChecks.class)
    public Integer age;
    @NotNull
    @AssertTrue(message = "必须具有驾照", groups = DriverChecks.class)
    public Boolean hasDrivingLicense;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public Driver() {
        super();
    }

    public Driver(String name) {
        super(name);
    }

    public Driver(int age, boolean hasDrivingLicense) {
        super();
        this.age = age;
        this.hasDrivingLicense = hasDrivingLicense;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getHasDrivingLicense() {
        return hasDrivingLicense;
    }

    public void setHasDrivingLicense(Boolean hasDrivingLicense) {
        this.hasDrivingLicense = hasDrivingLicense;
    }
}
