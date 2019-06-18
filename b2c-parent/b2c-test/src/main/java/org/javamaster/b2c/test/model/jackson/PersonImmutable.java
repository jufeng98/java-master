package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonImmutable {

    private long id;
    private String name;

    @JsonCreator
    public PersonImmutable(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}

