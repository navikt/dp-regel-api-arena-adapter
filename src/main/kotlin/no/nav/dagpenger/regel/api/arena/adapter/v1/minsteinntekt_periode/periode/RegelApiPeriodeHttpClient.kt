package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.TaskResponse
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeParametere

class RegelApiPeriodeHttpClient(private val regelApiUrl: String) {

    fun getPeriode(ressursUrl: String): PeriodeSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(PeriodeSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiTasksHttpClient.RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success -> result.get()
        }
    }

    fun startPeriodeSubsumsjon(payload: MinsteinntektOgPeriodeParametere): String {
        val url = "$regelApiUrl/periode"

        val jsonAdapter = moshiInstance.adapter(MinsteinntektOgPeriodeParametere::class.java)
        val json = jsonAdapter.toJson(payload)

        val (_, response, result) =
            with(url.httpPost()
                .header(mapOf("Content-Type" to "application/json"))
                .body(json)) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiTasksHttpClient.RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success ->
                response.headers["Location"]?.first() ?: throw RegelApiArenaAdapterException("No location")
        }
    }
}