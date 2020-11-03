package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.result.Result
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon

val sikkerlogg = KotlinLogging.logger("tjenestekall.subsumsjon")

internal class RegelApiSubsumsjonHttpClient(private val client: FuelHttpClient) {
    fun getSubsumsjon(subsumsjonLocation: String): Subsumsjon {

        val (_, response, result) = client.get<Subsumsjon>(subsumsjonLocation)

        return when (result) {
            is Result.Failure -> throw RegelApiSubsumsjonHttpClientException(
                "Failed to fetch subsumsjon. Response message: ${response.responseMessage}. Error message: ${result.error.message}"
            )
            is Result.Success -> {
                sikkerlogg.info { "Fikk svar på subsumsjon: ${result.get()}" }
                result.get()
            }
        }
    }
}

class RegelApiSubsumsjonHttpClientException(override val message: String) : RuntimeException(message)
