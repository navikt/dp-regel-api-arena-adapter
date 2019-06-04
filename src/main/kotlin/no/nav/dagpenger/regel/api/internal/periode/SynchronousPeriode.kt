package no.nav.dagpenger.regel.api.internal.periode

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.internal.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.internal.models.PeriodeSubsumsjon

class SynchronousPeriode(
    val regelApiPeriodeHttpClient: RegelApiPeriodeHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    suspend fun getPeriodeSynchronously(parametere: MinsteinntektOgPeriodeParametere): PeriodeSubsumsjon {

        val taskUrl = regelApiPeriodeHttpClient.startPeriodeSubsumsjon(parametere)

        val taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val periodeSubsumsjon = regelApiPeriodeHttpClient.getPeriode(ressursLocation)

        return periodeSubsumsjon
    }
}