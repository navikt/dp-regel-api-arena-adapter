package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.TaskStatus

class SynchronousSats(
    val regelApiSatsHttpClient: RegelApiSatsHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    fun getSatsSynchronously(parametere: GrunnlagOgSatsParametere): SatsSubsumsjon {

        val taskUrl = regelApiSatsHttpClient.startSatsSubsumsjon(parametere)

        var taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektSubsumsjon = regelApiSatsHttpClient.getSats(ressursLocation)

        return minsteinntektSubsumsjon
    }
}