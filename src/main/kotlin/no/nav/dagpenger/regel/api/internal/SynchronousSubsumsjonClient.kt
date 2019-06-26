package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.v1.SubsumsjonProblem
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import java.time.LocalDateTime

class SynchronousSubsumsjonClient(
    private val behovHttpClient: RegelApiBehovHttpClient,
    private val statusHttpClient: RegelApiStatusHttpClient,
    private val subsumsjonHttpClient: RegelApiSubsumsjonHttpClient
) {

    suspend fun <T> getSubsumsjonSynchronously(
        behovRequest: BehovRequest,
        extractResult: (subsumsjon: Subsumsjon, opprettet: LocalDateTime, utfort: LocalDateTime) -> T
    ): T {

        val opprettet = LocalDateTime.now()

        val statusUrl = behovHttpClient.run(behovRequest)
        val subsumsjonLocation = statusHttpClient.pollStatus(statusUrl)
        val subsumsjon = subsumsjonHttpClient.getSubsumsjon(subsumsjonLocation)

        val utfort = LocalDateTime.now()

        return subsumsjon.problem?.let { throw SubsumsjonProblem(it) }
            ?: extractResult(subsumsjon, opprettet, utfort)
    }
}