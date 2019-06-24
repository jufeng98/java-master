package org.javamaster.b2c.test.jackson;


import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.javamaster.b2c.test.model.jackson.Car;

/**
 * 类型处理模块
 *
 * @author yudong
 * @date 2019/6/18
 */
public class CarModule extends SimpleModule {
    public CarModule() {
        super(PackageVersion.VERSION);
        addDeserializer(Car.class, new CarDeserializer(Car.class));
        addSerializer(Car.class, new CarSerializer(Car.class));
    }
}
