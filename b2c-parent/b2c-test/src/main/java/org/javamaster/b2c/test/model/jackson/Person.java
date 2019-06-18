package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class Person {

    private long personId = 0;
    private String name;
    public Date birthday;

    private Map<String, Object> unreconized = new HashMap<>();

    @JsonAnySetter
    public void setUnreconized(String key, Object value) {
        unreconized.put(key, value);
    }

    public long getPersonId() {
        return this.personId;
    }

    @JsonSetter("id")
    public void setPersonId(long personId) {
        this.personId = personId;
    }

}