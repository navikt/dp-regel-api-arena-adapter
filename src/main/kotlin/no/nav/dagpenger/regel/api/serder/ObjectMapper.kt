package no.nav.dagpenger.regel.api.serder

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder

// java.time support is built into jackson-databind in Jackson 3 — no JavaTimeModule registration needed
internal val jacksonObjectMapper =
    jacksonMapperBuilder()
        // DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS now defaults to false — dates serialize as ISO-8601 strings already
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        .changeDefaultPropertyInclusion { it.withContentInclusion(JsonInclude.Include.NON_NULL) }
        .build()
