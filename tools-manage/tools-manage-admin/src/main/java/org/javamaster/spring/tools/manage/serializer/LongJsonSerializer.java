package org.javamaster.spring.tools.manage.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * long转换成string返回
 *
 * @author yudong
 * @date 2023/3/4
 */
public class LongJsonSerializer extends JsonSerializer<Long> {
    public static final LongJsonSerializer INSTANCE = new LongJsonSerializer();

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value + "");
        }
    }
}
