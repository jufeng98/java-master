package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Created by yu on 2018/3/22.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PersonAutoDetect {

    private long personId = 123;
    public String name = null;

}
