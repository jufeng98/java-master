package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JacksonInject;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonInject {
    private long id = 0;
    private String name = null;

    @JacksonInject
    private String source = null;

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
