package org.javamaster.b2c.test.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.javamaster.b2c.test.model.jackson.Car;

import java.io.IOException;

/**
 * 序列化器
 *
 * @author yudong
 * @date 2019/6/18
 */
public class CarSerializer extends StdSerializer<Car> {

    private static final long serialVersionUID = 2807109332342106505L;

    public CarSerializer(Class<Car> c) {
        super(c);
    }

    @Override
    public void serialize(Car car, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {

        jsonGenerator.writeStartObject();
        if (car.getBrand() != null) {
            jsonGenerator.writeStringField("brand", car.getBrand());
        } else {
            jsonGenerator.writeNullField("brand");
        }
        if (car.getDoors() != null) {
            jsonGenerator.writeNumberField("doors", car.getDoors());
        } else {
            jsonGenerator.writeNullField("doors");
        }
        jsonGenerator.writeEndObject();
    }
}
