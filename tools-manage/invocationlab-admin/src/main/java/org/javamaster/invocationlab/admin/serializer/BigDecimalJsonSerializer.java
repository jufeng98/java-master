package org.javamaster.invocationlab.admin.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * BigDecimal转换成string返回
 *
 * @author yudong
 * @date 2022/11/10
 */
public class BigDecimalJsonSerializer extends JsonSerializer<BigDecimal> {
    public static final BigDecimalJsonSerializer INSTANCE = new BigDecimalJsonSerializer();

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.toPlainString());
        }
    }
}
