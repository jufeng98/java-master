package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonIgnore {

    @JsonIgnore
    private long personId = 0;

    private String name = null;

    public PersonIgnore(long personId, String name) {
        this.personId = personId;
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
}