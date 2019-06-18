package org.javamaster.b2c.test.model.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * @author yudong
 * @date 2019/6/18
 */
@JsonIgnoreType
public class Address {
    private String streetName = null;
    private String houseNumber = null;
    private String zipCode = null;
    private String city = null;
    private String country = null;

    public Address(String country) {
        this.country = country;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
