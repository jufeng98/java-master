package org.javamaster.spring.swagger.model;

import org.javamaster.spring.swagger.enums.Sex;

import java.util.Arrays;

/**
 * @author yudong
 * @date 2022/4/18
 */
public class TestParam {
    private Integer age;
    private int age1;
    private Integer[] ages;
    private int[] ages1;
    private Sex sex;

    @Override
    public String toString() {
        return "TestParam{" +
                "age=" + age +
                ", age1=" + age1 +
                ", ages=" + Arrays.toString(ages) +
                ", ages1=" + Arrays.toString(ages1) +
                ", sex=" + sex +
                '}';
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public int getAge1() {
        return age1;
    }

    public void setAge1(int age1) {
        this.age1 = age1;
    }

    public Integer[] getAges() {
        return ages;
    }

    public void setAges(Integer[] ages) {
        this.ages = ages;
    }

    public int[] getAges1() {
        return ages1;
    }

    public void setAges1(int[] ages1) {
        this.ages1 = ages1;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }
}
