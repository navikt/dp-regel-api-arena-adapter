package no.nav.dagpenger.regel.api.internal

import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import java.time.LocalDateTime

class SynchronousSubsumsjonClient(
    private val behovHttpClient: RegelApiBehovHttpClient,
    private val statusHttpClient: RegelApiStatusHttpClient,
    private val subsumsjonHttpClient: RegelApiSubsumsjonHttpClient
) {

    fun <T> getSubsumsjonSynchronously(
        behovRequest: BehovRequest,
        extractResult: (subsumsjon: Subsumsjon, opprettet: LocalDateTime, utfort: LocalDateTime) -> T
    ): T {

        val opprettet = LocalDateTime.now()

        val statusUrl = behovHttpClient.run(behovRequest)
        val subsumsjonLocation = runBlocking { statusHttpClient.pollStatus(statusUrl) }
        val subsumsjon = subsumsjonHttpClient.getSubsumsjon(subsumsjonLocation)

        val utfort = LocalDateTime.now()

        return extractResult(subsumsjon, opprettet, utfort)
    }
}