package org.javamaster.invocationlab.admin.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import java.math.BigDecimal

/**
 * BigDecimal转换成string返回
 *
 * @author yudong
 * @date 2022/11/10
 */
class BigDecimalToJsonSerializer : JsonSerializer<BigDecimal?>() {

    override fun serialize(value: BigDecimal?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString(value.toPlainString())
        }
    }

    override fun serializeWithType(
        value: BigDecimal?,
        gen: JsonGenerator,
        serializers: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        val typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_NUMBER_FLOAT))
        serialize(value, gen, serializers)
        typeSer.writeTypeSuffix(gen, typeIdDef)
    }

    companion object {
        @JvmField
        val INSTANCE: BigDecimalToJsonSerializer = BigDecimalToJsonSerializer()
    }
}
