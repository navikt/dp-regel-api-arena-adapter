package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient

class SynchronousPeriode(
    val regelApiPeriodeHttpClient: RegelApiPeriodeHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    fun getPeriodeSynchronously(parametere: MinsteinntektOgPeriodeParametere): PeriodeSubsumsjon {

        val taskUrl = regelApiPeriodeHttpClient.startPeriodeSubsumsjon(parametere)

        val taskResponse = regelApiTasksHttpClient.pollTaskUntilDone(taskUrl)

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val periodeSubsumsjon = regelApiPeriodeHttpClient.getPeriode(ressursLocation)

        return periodeSubsumsjon
    }
}