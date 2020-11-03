package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import java.time.LocalDate

internal class RegelApiReberegningHttpClient(private val httpClient: FuelHttpClient) {
    private val jsonAdapter = moshiInstance.adapter(KreverReberegningParametre::class.java)

    fun kreverReberegning(subsumsjonIder: List<String>, beregningsdato: LocalDate): Boolean {
        val json = jsonAdapter.toJson(KreverReberegningParametre(beregningsdato, subsumsjonIder))

        val (_, response, result) =
            httpClient.post<KreverReberegningResponse>("/lovverk/krever-reberegning") {
                it.header("Content-Type" to "application/json")
                it.body(json)
            }

        return result.fold(
            { result.get().reberegning },
            {
                throw RegelApiReberegningHttpClientException(
                    "Failed to check reberegning Response message ${response.responseMessage}. Error message: ${it.message}. "
                )
            }
        )
    }

    private data class KreverReberegningResponse(val reberegning: Boolean)
    private data class KreverReberegningParametre(val beregningsdato: LocalDate, val subsumsjonIder: List<String>)
}

class RegelApiReberegningHttpClientException(override val message: String) : RuntimeException()
