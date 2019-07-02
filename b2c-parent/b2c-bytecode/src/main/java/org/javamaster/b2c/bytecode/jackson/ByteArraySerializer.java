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
public class ByteArraySerializer extends JsonSerializer<byte[]> {

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : value) {
                stringBuilder.append(Byte.toUnsignedInt(b)).append(" ");
            }
            gen.writeString(stringBuilder.toString());
        } else {
            gen.writeNull();
        }
    }
}
