package org.javamaster.b2c.test.model.validation;

import org.hibernate.validator.constraints.NotBlank;
import org.javamaster.b2c.test.model.jackson.Address;
import org.javamaster.b2c.test.validation.CheckCase;
import org.javamaster.b2c.test.validation.CheckPerson;

import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/17
 */
public class Person {
    private long personId = 0;
    @NotBlank
    @CheckCase(value = CheckCase.CaseMode.UPPER, message = "名字必须为大写")
    private String name;
    public Address address;
    public Date birthday;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}