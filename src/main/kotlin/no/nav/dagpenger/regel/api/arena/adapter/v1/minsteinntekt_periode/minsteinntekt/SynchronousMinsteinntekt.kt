package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.TaskStatus

class SynchronousMinsteinntekt(
    val regelApiMinsteinntektHttpClient: RegelApiMinsteinntektHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    fun getMinsteinntektSynchronously(parametere: MinsteinntektOgPeriodeParametere): MinsteinntektSubsumsjon {

        val taskUrl = regelApiMinsteinntektHttpClient.startMinsteinntektSubsumsjon(parametere)

        var taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektSubsumsjon = regelApiMinsteinntektHttpClient.getMinsteinntekt(ressursLocation)

        return minsteinntektSubsumsjon
    }
}