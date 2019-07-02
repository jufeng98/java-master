package org.javamaster.b2c.bytecode.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created on 2019/4/20.<br/>
 *
 * @author yudong
 */
public class HexSerializer extends JsonSerializer<Integer> {

    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value != null) {
            gen.writeNumber(Integer.toHexString(value).toUpperCase());
        } else {
            gen.writeNull();
        }
    }
}
