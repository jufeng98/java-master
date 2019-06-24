package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonGetter {

    private long personId = 0;

    @JsonGetter("id")
    public long getPersonId() {
        return personId;
    }


    @JsonSetter("id")
    public void setPersonId(long personId) {
        this.personId = personId;
    }
}
