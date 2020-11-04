package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import java.time.LocalDate

internal class RegelApiNyVurderingHttpClient(private val httpClient: FuelHttpClient) {
    private val jsonAdapter = moshiInstance.adapter(KreverNyVurderingParametre::class.java)

    fun kreverNyVurdering(subsumsjonIder: List<String>, beregningsdato: LocalDate): Boolean {
        val json = jsonAdapter.toJson(KreverNyVurderingParametre(beregningsdato, subsumsjonIder))

        val (_, response, result) =
            httpClient.post<KreverNyVurderingRespons>("/lovverk/vurdering/minsteinntekt") {
                it.header("Content-Type" to "application/json")
                it.body(json)
            }

        return result.fold(
            { result.get().nyVurdering },
            {
                throw RegelApiMinsteinntektNyVurderingException(
                    "Failed to check reberegning Response message ${response.responseMessage}. Error message: ${it.message}. "
                )
            }
        )
    }

    private data class KreverNyVurderingRespons(val nyVurdering: Boolean)
    private data class KreverNyVurderingParametre(val beregningsdato: LocalDate, val subsumsjonIder: List<String>)
}

internal class RegelApiMinsteinntektNyVurderingException(override val message: String) : RuntimeException()
