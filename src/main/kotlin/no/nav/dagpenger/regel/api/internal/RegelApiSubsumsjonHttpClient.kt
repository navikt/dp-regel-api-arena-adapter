package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon

val sikkerlogg = KotlinLogging.logger("tjenestekall")

class RegelApiSubsumsjonHttpClient(private val regelApiUrl: String, private val regelApiKey: String) {
    fun getSubsumsjon(subsumsjonLocation: String): Subsumsjon {
        val url = "$regelApiUrl$subsumsjonLocation"

        val jsonAdapter = moshiInstance.adapter(Subsumsjon::class.java)

        val (_, response, result) =
            with(
                url
                    .httpGet()
                    .apiKey(regelApiKey)
            ) { responseObject(moshiDeserializerOf(jsonAdapter)) }

        return when (result) {
            is Result.Failure -> throw RegelApiSubsumsjonHttpClientException(
                "Failed to fetch subsumsjon. Response message: ${response.responseMessage}. Error message: ${result.error.message}"
            )
            is Result.Success -> {
                // sikkerlogg.info { "Fikk svar på subsumsjon: ${result.get()}" }
                result.get()
            }
        }
    }
}

class RegelApiSubsumsjonHttpClientException(override val message: String) : RuntimeException(message)
