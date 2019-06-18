package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonRawValue {
    private long personId = 0;
    @JsonRawValue
    private String address = "{ \"street\" : \"Wall Street\", \"no\":1}";

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
