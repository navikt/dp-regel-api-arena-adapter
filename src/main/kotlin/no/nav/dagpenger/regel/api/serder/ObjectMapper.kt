package no.nav.dagpenger.regel.api.serder

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal val jacksonObjectMapper = jacksonObjectMapper().also {
    it.registerModule(JavaTimeModule())
//    it.registerModule(
//        SimpleModule().also { module ->
//            module.addSerializer(Ulid::class.java, UlidSerializer())
//            module.addDeserializer(Ulid::class.java, UlidDeserializer())
//        }
//    )
    it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    it.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    it.setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

//
// private class UlidSerializer : JsonSerializer<Ulid>() {
//    override fun serialize(value: Ulid, gen: JsonGenerator, serializers: SerializerProvider) =
//        gen.writeString(value.id)
// }
//
// private class UlidDeserializer : JsonDeserializer<Ulid>() {
//    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Ulid = Ulid(p.text)
// }
