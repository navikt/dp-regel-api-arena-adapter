package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient

class SynchronousMinsteinntekt(
    val regelApiMinsteinntektHttpClient: RegelApiMinsteinntektHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    fun getMinsteinntektSynchronously(parametere: MinsteinntektOgPeriodeParametere): MinsteinntektSubsumsjon {

        val taskUrl = regelApiMinsteinntektHttpClient.startMinsteinntektSubsumsjon(parametere)

        val taskResponse = regelApiTasksHttpClient.pollTaskUntilDone(taskUrl)

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektSubsumsjon = regelApiMinsteinntektHttpClient.getMinsteinntekt(ressursLocation)

        return minsteinntektSubsumsjon
    }
}