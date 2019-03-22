package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.SatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient

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