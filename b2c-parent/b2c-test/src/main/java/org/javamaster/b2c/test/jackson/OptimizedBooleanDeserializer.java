package org.javamaster.b2c.test.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author yudong
 * @date 2018/3/22
 */
public class OptimizedBooleanDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        String text = jsonParser.getText();
        if ("0".equals(text)) {
            return false;
        } else {
            return true;
        }
    }
}
