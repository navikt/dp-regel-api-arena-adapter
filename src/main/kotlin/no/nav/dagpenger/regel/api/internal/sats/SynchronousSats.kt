package no.nav.dagpenger.regel.api.internal.sats

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.internal.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.internal.models.SatsSubsumsjon

class SynchronousSats(
    val regelApiSatsHttpClient: RegelApiSatsHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    suspend fun getSatsSynchronously(parametere: GrunnlagOgSatsParametere): SatsSubsumsjon {

        val taskUrl = regelApiSatsHttpClient.startSatsSubsumsjon(parametere)

        val taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektSubsumsjon = regelApiSatsHttpClient.getSats(ressursLocation)

        return minsteinntektSubsumsjon
    }
}