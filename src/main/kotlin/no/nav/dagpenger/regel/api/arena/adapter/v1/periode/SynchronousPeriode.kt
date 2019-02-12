package no.nav.dagpenger.regel.api.arena.adapter.v1.periode

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.RegelApiHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.TaskStatus
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektInnParametere

class SynchronousPeriode(val regelApiHttpClient: RegelApiHttpClient) {

    fun getPeriodeSynchronously(parametere: MinsteinntektInnParametere): PeriodeSubsumsjon {

        val taskUrl = regelApiHttpClient.startPeriodeSubsumsjon(parametere)

        var taskResponse = regelApiHttpClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiHttpClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val periodeSubsumsjon = regelApiHttpClient.getPeriode(ressursLocation)

        return periodeSubsumsjon
    }
}