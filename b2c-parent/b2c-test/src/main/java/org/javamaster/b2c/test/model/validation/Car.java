package org.javamaster.b2c.test.model.validation;

import org.hibernate.validator.constraints.Range;
import org.javamaster.b2c.test.validation.CarChecks;
import org.javamaster.b2c.test.validation.CarTypeEnum;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 * @date 2019/6/17
 */
public class Car {
    @NotBlank // 不能为null,不能为空字符串
    private String manufacturer;
    @NotNull // 不能为null
    @Size(min = 2, max = 14) // 字符串长度位于2到14之间
    private String licensePlate;
    @Min(2)
    @Max(5) // 注意,未添加NotNull注解,所以seatCount可以为null,只有当seatCount不为null@Min @Max才会做校验
    private Integer seatCount;
    @AssertTrue // registered不为null时,则值需是true
    private Boolean registered;

    @AssertTrue(message = "The car has to pass the vehicle inspection first", groups = CarChecks.class)
    private Boolean passedVehicleInspection;

    @Valid // 表明应对driver对象内字段继续做校验
    @NotNull
    private Driver driver;

    @Valid // 表明应对passengers里的Person对象内字段继续做校验
    @Size(max = 2) // 表明passengers最多只能有两个对象
    private List<Person> passengers = new ArrayList<>();

    private String brand;


    @Range(min = 2,max = 4,message = "车门数量必须位于${min}和${max}之间,当前值为:${validatedValue}") // 作用同@Min @Max
    private Integer doors;

    private CarTypeEnum carTypeEnum;

    public Car() {
        super();
    }

    public Car(String manufacturer, String licensePlate, int seatCount, boolean registered,
               boolean passedVehicleInspection, Driver driver, List<Person> passengers) {
        super();
        this.manufacturer = manufacturer;
        this.licensePlate = licensePlate;
        this.seatCount = seatCount;
        this.registered = registered;
        this.passedVehicleInspection = passedVehicleInspection;
        this.driver = driver;
        this.passengers = passengers;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isPassedVehicleInspection() {
        return passedVehicleInspection;
    }

    public void setPassedVehicleInspection(boolean passedVehicleInspection) {
        this.passedVehicleInspection = passedVehicleInspection;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public List<Person> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Person> passengers) {
        this.passengers = passengers;
    }

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

    public CarTypeEnum getCarTypeEnum() {
        return carTypeEnum;
    }

    public void setCarTypeEnum(CarTypeEnum carTypeEnum) {
        this.carTypeEnum = carTypeEnum;
    }
}
