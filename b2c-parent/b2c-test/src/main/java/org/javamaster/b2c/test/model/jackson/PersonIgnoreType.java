package org.javamaster.b2c.test.model.jackson;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class PersonIgnoreType {
    private long personId = 0;
    private String name = null;
    private Address address = null;

    public PersonIgnoreType(long personId, String name, Address address) {
        this.personId = personId;
        this.name = name;
        this.address = address;
    }

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
