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
        val totalTimer = clientLatencyStats.labels("total").startTimer()

        val createBehovTimer = clientLatencyStats.labels("create").startTimer()
        val statusUrl = behovHttpClient.run(behovRequest)
        createBehovTimer.observeDuration()

        val pollBehovTimer = clientLatencyStats.labels("poll_total").startTimer()
        val subsumsjonLocation = statusHttpClient.pollStatus(statusUrl)
        pollBehovTimer.observeDuration()

        val resultOfBehovTimer = clientLatencyStats.labels("result").startTimer()
        val subsumsjon = subsumsjonHttpClient.getSubsumsjon(subsumsjonLocation)
        resultOfBehovTimer.observeDuration()

        val utfort = LocalDateTime.now()
        totalTimer.observeDuration()

        return subsumsjon.problem?.let { throw SubsumsjonProblem(it) }
            ?: extractResult(subsumsjon, opprettet, utfort)
    }
}
