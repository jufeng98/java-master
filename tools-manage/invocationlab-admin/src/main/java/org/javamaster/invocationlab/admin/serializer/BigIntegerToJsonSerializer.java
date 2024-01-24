package org.javamaster.invocationlab.admin.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.math.BigInteger;

/**
 * BigInteger转换成string返回
 *
 * @author yudong
 */
public class BigIntegerToJsonSerializer extends JsonSerializer<BigInteger> {
    public static final BigIntegerToJsonSerializer INSTANCE = new BigIntegerToJsonSerializer();

    @Override
    public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.toString());
        }
    }

    @Override
    public void serializeWithType(BigInteger value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_NUMBER_INT));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

}
