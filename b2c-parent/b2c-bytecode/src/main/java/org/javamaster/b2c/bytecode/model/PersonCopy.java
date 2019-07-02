package org.javamaster.b2c.bytecode.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/27
 */
@Deprecated
public class PersonCopy<K, U extends Number> implements Serializable {

    private static final long serialVersionUID = 2700105470800398661L;

    private String name;

    private Integer age;

    @JsonProperty(value = "newData")
    public Map<K, U> data;

    public static final Integer VALUE = 88;

    @JsonGetter
    public int increaseAge(Integer theMaxValues) throws FileNotFoundException {
        try {

            new InnerClass() {
            };

            return age + theMaxValues;
        } catch (Exception e) {
            return -1;
        }
    }

    public String getName() {
        return name;
    }

    public Map<K, U> getData() {
        return data;
    }

    class InnerClass {
    }
}
