package org.javamaster.b2c.test.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.javamaster.b2c.test.model.jackson.Car;

import java.io.IOException;

/**
 * 反序列化器
 *
 * @author yudong
 * @date 2019/6/18
 */
public class CarDeserializer extends StdDeserializer<Car> {

    private static final long serialVersionUID = 4977601024588834191L;

    public CarDeserializer(Class<?> c) {
        super(c);
    }

    @Override
    public Car deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        Car car = new Car();
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME == jsonToken) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                if ("doors".equals(fieldName)) {
                    car.setDoors(parser.getValueAsInt());
                } else if ("brand".equals(fieldName)) {
                    car.setBrand(parser.getValueAsString());
                }
            }
        }
        return car;
    }
}
