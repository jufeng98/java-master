package org.javamaster.b2c.test.java8;

import org.javamaster.b2c.test.model.Car;
import org.javamaster.b2c.test.model.Insurance;
import org.javamaster.b2c.test.model.Person;
import org.junit.Test;

import java.util.Optional;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class OptionalTest extends CommonCode {

    @Test
    public void test() {

    }

    public String getCarInsuranceName(Person person) {
        return person.getCar().getInsurance().getName();
    }

    public String getCarInsuranceName1(Person person) {
        if (person != null) {
            Car car = person.getCar();
            if (car != null) {
                Insurance insurance = car.getInsurance();
                if (insurance != null) {
                    //  这里就不需要做空检查了,一旦引用insurance公司名称时发生NullPointerException ，就能非常确定地
                    //  知道出错的原因，不再需要为其添加null的检查，因为null的检查只会掩盖问题，并未真正地修复问题。
                    //  保险公司必定有个名字，所以，如果你遇到一个公司没有名称，你需要调查你的数据出了什么问题，而不
                    //  应该再添加一段代码，将这个问题隐藏。
                    return insurance.getName();

                }
            }
        }
        return "Unknown";
    }

    public String getCarInsuranceName3(Person person) {
        Optional<Person> optPerson = Optional.ofNullable(person);
        String name = optPerson
                .map(Person::getCar)
                .map(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("UnKnown"); // 如果Optional的结果值为空，设置默认值
        return name;
    }
}
