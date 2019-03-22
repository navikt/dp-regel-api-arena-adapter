package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient

class SynchronousGrunnlag(
    val regelApiGrunnlagHttpClient: RegelApiGrunnlagHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    suspend fun getGrunnlagSynchronously(parametere: GrunnlagOgSatsParametere): GrunnlagSubsumsjon {

        val taskUrl = regelApiGrunnlagHttpClient.startGrunnlagSubsumsjon(parametere)

        val taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val grunnlagSubsumsjon = regelApiGrunnlagHttpClient.getGrunnlag(ressursLocation)

        return grunnlagSubsumsjon
    }
}