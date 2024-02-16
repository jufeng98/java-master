package org.javamaster.invocationlab.admin.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
class ByteArrayToJsonSerializer : JsonSerializer<ByteArray?>() {

    override fun serialize(value: ByteArray?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString("BINARY," + value.size + " Bytes(" + String(value, StandardCharsets.UTF_8) + ")")
        }
    }
}
