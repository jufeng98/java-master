package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.javamaster.b2c.test.jackson.OptimizedBooleanDeserializer;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonDeserialize {

    private long id = 0;
    private String name = null;
    private int age;
    @JsonDeserialize(using = OptimizedBooleanDeserializer.class)
    private boolean enabled = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
