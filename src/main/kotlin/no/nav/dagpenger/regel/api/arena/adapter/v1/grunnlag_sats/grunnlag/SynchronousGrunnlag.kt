package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.TaskStatus

class SynchronousGrunnlag(
    val regelApiGrunnlagHttpClient: RegelApiGrunnlagHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    fun getGrunnlagSynchronously(parametere: GrunnlagOgSatsParametere): GrunnlagSubsumsjon {

        val taskUrl = regelApiGrunnlagHttpClient.startGrunnlagSubsumsjon(parametere)

        var taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektSubsumsjon = regelApiGrunnlagHttpClient.getGrunnlag(ressursLocation)

        return minsteinntektSubsumsjon
    }
}