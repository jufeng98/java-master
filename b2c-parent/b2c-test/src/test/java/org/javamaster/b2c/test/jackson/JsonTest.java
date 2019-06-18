package org.javamaster.b2c.test.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javamaster.b2c.test.model.jackson.Address;
import org.javamaster.b2c.test.model.jackson.Car;
import org.javamaster.b2c.test.model.jackson.EcardUserInformation;
import org.javamaster.b2c.test.model.jackson.LoginUserInformation;
import org.javamaster.b2c.test.model.jackson.MemberInformation;
import org.javamaster.b2c.test.model.jackson.PersonAnyGetter;
import org.javamaster.b2c.test.model.jackson.PersonDeserialize;
import org.javamaster.b2c.test.model.jackson.PersonGetter;
import org.javamaster.b2c.test.model.jackson.PersonIgnore;
import org.javamaster.b2c.test.model.jackson.PersonIgnoreProperties;
import org.javamaster.b2c.test.model.jackson.PersonIgnoreType;
import org.javamaster.b2c.test.model.jackson.PersonImmutable;
import org.javamaster.b2c.test.model.jackson.PersonInclude;
import org.javamaster.b2c.test.model.jackson.PersonInject;
import org.javamaster.b2c.test.model.jackson.PersonPropertyOrder;
import org.javamaster.b2c.test.model.jackson.PersonRawValue;
import org.javamaster.b2c.test.model.jackson.PersonSerializer;
import org.javamaster.b2c.test.model.jackson.PersonValue;
import org.javamaster.b2c.test.model.jackson.Transaction;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author yudong
 * @date 2019/6/18
 */
public class JsonTest {
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
        // 从字符串创建
        Car car = objectMapper.readValue(carJson, Car.class);
        System.out.println(objectMapper.writeValueAsString(car));

        // 从Reader创建
        Reader reader = new StringReader(carJson);
        car = objectMapper.readValue(reader, Car.class);
        System.out.println(objectMapper.writeValueAsString(car));

        File file = ResourceUtils.getFile("classpath:car.json");
        // 从文件创建
        car = objectMapper.readValue(file, Car.class);
        System.out.println(objectMapper.writeValueAsString(car));

        URL url = ResourceUtils.getFile("classpath:car.json").toURI().toURL();
        // 从URL创建
        car = objectMapper.readValue(url, Car.class);
        System.out.println(objectMapper.writeValueAsString(car));

        InputStream input = new FileInputStream(file);
        // 从InputStream创建
        car = objectMapper.readValue(input, Car.class);
        System.out.println(objectMapper.writeValueAsString(car));

        String jsonArray = "[{\"brand\":\"ford\"}, {\"brand\":\"Fiat\"}]";
        // 解析为数组
        Car[] cars = objectMapper.readValue(jsonArray, Car[].class);
        System.out.println(objectMapper.writeValueAsString(cars));

        // 解析为list
        List<Car> carList = objectMapper.readValue(jsonArray, new TypeReference<List<Car>>() {
        });
        System.out.println(objectMapper.writeValueAsString(carList));

        String jsonObjectStr = "{\"brand\":\"ford\", \"doors\":5}";
        // 解析为Map
        Map<String, Object> map = objectMapper.readValue(jsonObjectStr,
                new TypeReference<Map<String, Object>>() {
                });
        System.out.println(objectMapper.writeValueAsString(map));

    }


    @Test
    public void test1() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setCreate(new Date());
        transaction.setDate(new Date());
        String resJson = objectMapper.writeValueAsString(transaction);
        // 默认把时间序列化为long
        System.out.println(resJson);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 设置这个会影响所有Bean的Date序列化和反序列化
        objectMapper.setDateFormat(dateFormat);

        resJson = objectMapper.writeValueAsString(transaction);
        System.out.println(resJson);

        String transactionJsonStr = "{\"type\":\"transfer\",\"date\":\"20190118 14:27:52052\",\"create\":\"2019-01-18 14:27:52\"," +
                "\"transactionTypeEnum\":\"TICKET\",\"transactionType\":2,\"carTypeEnum\":3}";
        transaction = objectMapper.readValue(transactionJsonStr, Transaction.class);
        System.out.println(transaction);
    }

    @Test
    public void test2() throws Exception {
        String carJson =
                "{ \"brand\" : \"Mercedes\", \"doors\" : 5," +
                        " \"owners\" : [\"John\", \"Jack\", \"Jill\"]," +
                        " \"nestedObject\" : { \"field\" : \"value\" } }";
        JsonNode jsonNode = objectMapper.readValue(carJson, JsonNode.class);
        // 或者
        jsonNode = objectMapper.readTree(carJson);

        // 取JSON属性值
        JsonNode brandNode = jsonNode.get("brand");
        String brand = brandNode.asText();
        System.out.println("brand = " + brand);
        JsonNode doorsNode = jsonNode.get("doors");
        int doors = doorsNode.asInt();
        System.out.println("doors = " + doors);

        // 取JSON数组
        JsonNode jsonArray = jsonNode.get("owners");
        JsonNode jsonArrayNode = jsonArray.get(0);
        String john = jsonArrayNode.asText();
        System.out.println("john = " + john);

        // 取JSON内嵌对象
        JsonNode childNode = jsonNode.get("nestedObject");
        JsonNode childField = childNode.get("field");
        String field = childField.asText();
        System.out.println("field = " + field);
    }

    @Test
    public void test3() throws Exception {
        // 创建ObjectNode
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("brand", "Mercedes");
        objectNode.put("doors", 5);
        ObjectNode nestNode = objectMapper.createObjectNode();
        nestNode.put("field", "value");
        objectNode.set("nestedObject", nestNode);
        System.out.println(objectMapper.writeValueAsString(objectNode));

    }


    @Test
    public void test5() throws Exception {
        // 各类注解的具体用法
        PersonIgnore personIgnore = new PersonIgnore(1, "John");
        // {"name":"John"}
        System.out.println(objectMapper.writeValueAsString(personIgnore));

        PersonIgnoreProperties personIgnoreProperties = new PersonIgnoreProperties(1, "John", "Rod");
        // {"personId":1}
        System.out.println(objectMapper.writeValueAsString(personIgnoreProperties));

        PersonIgnoreType obj = new PersonIgnoreType(1, "John", new Address("China"));
        // {"personId":1,"name":"John"}
        System.out.println(objectMapper.writeValueAsString(obj));

        String s = "{\"id\" : 234432, \"name\" : \"liang yudong\",\"age\":22,\"enabled\":\"0\"}";
        PersonImmutable personImmutable = objectMapper.readValue(s, PersonImmutable.class);
        System.out.println(objectMapper.writeValueAsString(personImmutable));

        InjectableValues inject = new InjectableValues.Std().addValue(String.class, "baidu.com");
        PersonInject personInject = objectMapper.reader(inject)
                .forType(PersonInject.class)
                .readValue(s);
        System.out.println(objectMapper.writeValueAsString(personInject));

        PersonDeserialize personDeserialize = objectMapper.readValue(s, PersonDeserialize.class);
        System.out.println(objectMapper.writeValueAsString(personDeserialize));

        PersonInclude personInclude = new PersonInclude();
        System.out.println(objectMapper.writeValueAsString(personInclude));

        PersonGetter personGetter = new PersonGetter();
        System.out.println(objectMapper.writeValueAsString(personGetter));

        PersonAnyGetter personAnyGetter = new PersonAnyGetter();
        personAnyGetter.getProperties().put("name", "Jack");
        personAnyGetter.getProperties().put("age", 18);
        personAnyGetter.getProperties().put("enabled", "0");
        System.out.println(objectMapper.writeValueAsString(personAnyGetter));

        PersonPropertyOrder personPropertyOrder = new PersonPropertyOrder();
        System.out.println(objectMapper.writeValueAsString(personPropertyOrder));

        PersonRawValue personRawValue = new PersonRawValue();
        System.out.println(objectMapper.writeValueAsString(personRawValue));

        PersonValue personValue = new PersonValue();
        System.out.println(objectMapper.writeValueAsString(personValue));

        PersonSerializer personSerializer = new PersonSerializer();
        System.out.println(objectMapper.writeValueAsString(personSerializer));

        LoginUserInformation loginUserInformation = new EcardUserInformation();
        ((EcardUserInformation) loginUserInformation).setAid("124124124");
        System.out.println(objectMapper.writeValueAsString(loginUserInformation));

        LoginUserInformation memberInformation = new MemberInformation();
        ((MemberInformation) memberInformation).setPasswd("qq123123");
        System.out.println(objectMapper.writeValueAsString(memberInformation));

        String jsonStr = "{\"@class\":\"org.javamaster.b2c.core.model.jackson.MemberInformation\",\"userType\":\"1\"," +
                "\"ip\":\"127.0.0.1\",\"fpcardno\":\"513712340023\",\"passwd\":\"qq123123\"}";
        memberInformation = objectMapper.readValue(jsonStr, LoginUserInformation.class);
        System.out.println(objectMapper.writeValueAsString(memberInformation));
    }

    @Test
    public void test6() throws IOException {
        // 完全定制类的序列化和反序列化过程
        SimpleModule carModule = new CarModule();
        // 注册针对这个类型的处理模块
        objectMapper.registerModule(carModule);
        Car car = new Car();
        car.setBrand("BMW");
        car.setDoors(4);
        String json = objectMapper.writeValueAsString(car);
        System.out.println(json);
        car = objectMapper.readValue(json, Car.class);
        System.out.println(objectMapper.writeValueAsString(car));
    }

}

