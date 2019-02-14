package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.TaskStatus
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeParametere

class SynchronousPeriode(
    val regelApiPeriodeHttpClient: RegelApiPeriodeHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    fun getPeriodeSynchronously(parametere: MinsteinntektOgPeriodeParametere): PeriodeSubsumsjon {

        val taskUrl = regelApiPeriodeHttpClient.startPeriodeSubsumsjon(parametere)

        var taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val periodeSubsumsjon = regelApiPeriodeHttpClient.getPeriode(ressursLocation)

        return periodeSubsumsjon
    }
}