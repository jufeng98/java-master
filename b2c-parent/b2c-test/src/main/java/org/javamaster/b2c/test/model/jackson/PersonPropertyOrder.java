package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author yudong
 * @date 2019/6/18
 */
@JsonPropertyOrder({"name", "personId"})
public class PersonPropertyOrder {

    private long personId = 0;
    private String name = null;

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
