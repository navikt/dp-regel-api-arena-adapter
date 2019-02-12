package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.v1.RegelApiHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.TaskStatus
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektInnParametere

class SynchronousMinsteinntekt(val regelApiHttpClient: RegelApiHttpClient) {

    fun getMinsteinntektSynchronously(parametere: MinsteinntektInnParametere): MinsteinntektSubsumsjon {

        val taskUrl = regelApiHttpClient.startMinsteinntektSubsumsjon(parametere)

        var taskResponse = regelApiHttpClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiHttpClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektSubsumsjon = regelApiHttpClient.getMinsteinntekt(ressursLocation)

        return minsteinntektSubsumsjon
    }
}