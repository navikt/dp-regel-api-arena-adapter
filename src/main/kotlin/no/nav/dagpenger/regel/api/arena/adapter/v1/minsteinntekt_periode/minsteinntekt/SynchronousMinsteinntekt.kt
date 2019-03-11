package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient

class SynchronousMinsteinntekt(
    val regelApiMinsteinntektHttpClient: RegelApiMinsteinntektHttpClient,
    val regelApiTasksHttpClient: RegelApiTasksHttpClient
) {

    suspend fun getMinsteinntektSynchronously(parametere: MinsteinntektOgPeriodeParametere): MinsteinntektSubsumsjon {

        val taskUrl = regelApiMinsteinntektHttpClient.startMinsteinntektSubsumsjon(parametere)

        val taskResponse = regelApiTasksHttpClient.pollTask(taskUrl)

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val internalMinsteinntektSubsumsjon = regelApiMinsteinntektHttpClient.getMinsteinntekt(ressursLocation)

        // mappe

        return internalMinsteinntektSubsumsjon
    }
}