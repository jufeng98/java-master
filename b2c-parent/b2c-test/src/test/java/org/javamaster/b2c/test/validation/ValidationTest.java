package org.javamaster.b2c.test.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.javamaster.b2c.test.model.jackson.Address;
import org.javamaster.b2c.test.model.validation.Car;
import org.javamaster.b2c.test.model.validation.Driver;
import org.javamaster.b2c.test.model.validation.Person;
import org.javamaster.b2c.test.model.validation.RentalCar;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author yudong
 * @date 2019/6/17
 */
public class ValidationTest {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void test() {
        Car car = new Car();
        car.setManufacturer("benz");
        car.setLicensePlate("234234");
        car.setSeatCount(5);
        car.setRegistered(true);
        car.setPassedVehicleInspection(true);
        Driver driver = new Driver();
        driver.setName("JACK");
        driver.setAge(11);
        driver.setHasDrivingLicense(false);
        car.setDriver(driver);
        car.setPassengers(new ArrayList<>());
        car.setBrand("");
        car.setDoors(4);
        car.setCarTypeEnum(CarTypeEnum.BENZ);
        // 不传递校验顺序,则只校验group为Default的,没有显式在注解写明groups属性的,则默认为Default
        validateBean(car);
        // 校验顺序显式传递
        validateBean(car, OrederedChecks.class);

        car.getDriver().setAge(20);
        car.getDriver().setHasDrivingLicense(true);
        validateBean(car, OrederedChecks.class);

        car.setSeatCount(1);
        Set<ConstraintViolation<Car>> constraintViolations3 = validator.validateProperty(car, "seatCount");
        System.err.println(constraintViolations3.iterator().next().getMessage());

        Set<ConstraintViolation<Car>> constraintViolations4 = validator.validateValue(Car.class, "registered", false);
        System.err.println(constraintViolations4.iterator().next().getMessage());

        // 校验顺序写在bean的类注解上
        RentalCar rentalCar = new RentalCar();
        Set<ConstraintViolation<RentalCar>> constraintViolations5 = validator.validate(rentalCar);
        System.err.println(constraintViolations5.iterator().next().getMessage());

    }

    @Test
    public void test1() {
        // 测试自定义校验注解
        Person person = new Person("John");
        validateBean(person);
        person.setName("JOHN");
        validateBean(person);
        Address address = new Address("tianhe");
        address.setCity("GZ");
        person.setAddress(address);
        validateBean(person);
    }

    public static <T> void validateBean(T bean, Class<?>... groups) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(bean, groups);
        if (constraintViolations.isEmpty()) {
            System.out.println("校验通过");
            return;
        }
        List<String> errors = new ArrayList<>(10);
        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            errors.add(constraintViolation.getPropertyPath() + constraintViolation.getMessage());
        }
        //throw new ValidationException(StringUtils.join(errors, ","));
        System.err.println(StringUtils.join(errors, ","));
    }
}
