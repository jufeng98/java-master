package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonAnyGetter {
    private Map<String, Object> properties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> properties() {
        return properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
