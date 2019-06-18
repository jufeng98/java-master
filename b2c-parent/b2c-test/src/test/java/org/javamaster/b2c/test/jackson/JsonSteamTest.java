package org.javamaster.b2c.test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.javamaster.b2c.test.model.jackson.Car;
import org.junit.Test;

import java.io.File;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class JsonSteamTest {
    static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Test
    public void test() throws Exception {
        String carJson = "{ \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(carJson);
        //START_OBJECT
        //END_OBJECT
        //START_ARRAY
        //END_ARRAY
        //FIELD_NAME
        //VALUE_EMBEDDED_OBJECT
        //VALUE_FALSE
        //VALUE_TRUE
        //VALUE_NULL
        //VALUE_STRING
        //VALUE_NUMBER_INT
        //VALUE_NUMBER_FLOAT
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            System.out.println("jsonToken = " + jsonToken);
        }
    }

    @Test
    public void test1() throws Exception {
        String carJson = "{ \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(carJson);
        Car car = new Car();
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = parser.getCurrentName();
                System.out.println(fieldName);
                jsonToken = parser.nextToken();
                if ("brand".equals(fieldName)) {
                    car.setBrand(parser.getValueAsString());
                } else if ("doors".equals(fieldName)) {
                    car.setDoors(parser.getValueAsInt());
                }
            }
        }
        System.out.println("car.brand = " + car);
    }

    @Test
    public void test2() throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(
                new File("D:\\output.json"), JsonEncoding.UTF8);
        generator.writeStartObject();
        generator.writeStringField("brand", "Mercedes");
        generator.writeNumberField("doors", 5);
        generator.writeEndObject();
        generator.close();
    }

}

