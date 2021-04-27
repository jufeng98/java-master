package org.javamaster.b2c.test.model.validation;

import org.javamaster.b2c.test.model.jackson.Address;
import org.javamaster.b2c.test.validation.CheckCase;
import org.javamaster.b2c.test.validation.CheckPerson;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/17
 */
@CheckPerson(name = "JOHN", city = "GZ", message = "地址信息有误")
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

    public Person(@NotBlank String name, @NotNull @Past Date birthday) {
        this.name = name;
        this.birthday = birthday;
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

    @NotNull
    @Past
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(@NotNull @Past Date birthday) {
        this.birthday = birthday;
    }
}