package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author yudong
 * @date 2019/6/18
 */
@JsonIgnoreProperties({"firstName", "lastName"})
public class PersonIgnoreProperties {
    private long personId = 0;
    private String firstName = null;
    private String lastName = null;

    public PersonIgnoreProperties(long personId, String firstName, String lastName) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
