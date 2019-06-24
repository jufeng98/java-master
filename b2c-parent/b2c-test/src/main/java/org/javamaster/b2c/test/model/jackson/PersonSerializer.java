package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.javamaster.b2c.test.jackson.OptimizedBooleanSerializer;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonSerializer {

    private long personId = 0;
    private String name = "John";
    @JsonSerialize(using = OptimizedBooleanSerializer.class)
    private boolean enabled = true;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
