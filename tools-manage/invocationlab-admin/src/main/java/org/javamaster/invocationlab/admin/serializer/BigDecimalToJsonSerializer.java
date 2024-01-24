package org.javamaster.invocationlab.admin.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * BigDecimal转换成string返回
 *
 * @author yudong
 * @date 2022/11/10
 */
public class BigDecimalToJsonSerializer extends JsonSerializer<BigDecimal> {
    public static final BigDecimalToJsonSerializer INSTANCE = new BigDecimalToJsonSerializer();

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.toPlainString());
        }
    }

    @Override
    public void serializeWithType(BigDecimal value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_NUMBER_FLOAT));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

}
