package org.javamaster.invocationlab.admin.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 */
public class ByteArrayToJsonSerializer extends JsonSerializer<byte[]> {
    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString("BINARY," + value.length + " Bytes(" + new String(value, StandardCharsets.UTF_8) + ")");
        }
    }
}
