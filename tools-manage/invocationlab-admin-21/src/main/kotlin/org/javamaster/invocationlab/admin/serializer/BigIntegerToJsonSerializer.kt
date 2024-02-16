package org.javamaster.invocationlab.admin.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import java.math.BigInteger

/**
 * BigInteger转换成string返回
 *
 * @author yudong
 */
class BigIntegerToJsonSerializer : JsonSerializer<BigInteger?>() {

    override fun serialize(value: BigInteger?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString(value.toString())
        }
    }

    override fun serializeWithType(
        value: BigInteger?,
        gen: JsonGenerator,
        serializers: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        val typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_NUMBER_INT))
        serialize(value, gen, serializers)
        typeSer.writeTypeSuffix(gen, typeIdDef)
    }

    companion object {
        val INSTANCE: BigIntegerToJsonSerializer = BigIntegerToJsonSerializer()
    }
}
