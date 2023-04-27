package no.nav.dagpenger.regel.api.serder

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal val jacksonObjectMapper = jacksonObjectMapper().also {
    it.registerModule(JavaTimeModule())
    it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    it.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    it.setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
