package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.result.Result
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon

private val sikkerlogg = KotlinLogging.logger("tjenestekall.subsumsjon")

internal class RegelApiSubsumsjonHttpClient(private val client: FuelHttpClient) {
    fun getSubsumsjon(subsumsjonLocation: String): Subsumsjon {

        val (_, response, result) = client.get<Subsumsjon>(subsumsjonLocation)

        return when (result) {
            is Result.Failure -> throw RegelApiSubsumsjonHttpClientException(
                "Failed to fetch subsumsjon. Response message: ${response.responseMessage}. Error message: ${result.error.message}"
            )
            is Result.Success -> {
                result.get().also {
                    withLoggingContext(
                        "requestId" to it.requestId.toString(),
                        "behovId" to it.behovId,
                        "aktorId" to it.faktum.aktorId,
                        "vedtakId" to it.faktum.regelkontekst.id,
                        "inntektsId" to it.faktum.inntektsId.toString(),
                    ) {
                        sikkerlogg.info { "Fikk svar på subsumsjon: $it" }
                    }
                }
            }
        }
    }
}

class RegelApiSubsumsjonHttpClientException(override val message: String) : RuntimeException(message)
