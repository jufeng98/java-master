package org.javamaster.b2c.test.model.jackson;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class Car {

    private String brand;
    private Integer doors;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getDoors() {
        return doors;
    }

    public void setDoors(Integer doors) {
        this.doors = doors;
    }
}
