package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonValue {
    private long personId = 0;
    private String name = null;

    @JsonValue
    public String toJson() {
        return this.personId + "," + this.name;
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
