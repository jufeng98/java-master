package org.javamaster.b2c.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.javamaster.b2c.core.enums.EnumBase;

import java.io.IOException;

/**
 * 用来序列化实现了EnumBase接口的枚举类
 *
 * @author yudong
 * @date 2019/6/10
 */
public class EnumBaseSerializer extends JsonSerializer<EnumBase> {

    @Override
    public void serialize(EnumBase value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value != null) {
            gen.writeNumber(value.getCode());
        } else {
            gen.writeNull();
        }
    }
}
