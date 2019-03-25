package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.TaskResponse
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.MinsteinntektSubsumsjon

class RegelApiMinsteinntektHttpClient(private val regelApiUrl: String) {

    fun startMinsteinntektSubsumsjon(payload: MinsteinntektOgPeriodeParametere): String {
        val url = "$regelApiUrl/minsteinntekt"

        val jsonAdapter = moshiInstance.adapter(MinsteinntektOgPeriodeParametere::class.java)
        val json = jsonAdapter.toJson(payload)

        val (_, response, result) =
            with(
                url.httpPost()
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)
            ) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiMinsteinntektHttpClientException(
                "Failed to run minsteinntekt. Response message ${response.responseMessage}. Error message: ${result.error.message}")
            is Result.Success ->
                response.headers["Location"].first()
        }
    }

    fun getMinsteinntekt(ressursUrl: String): MinsteinntektSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(MinsteinntektSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiMinsteinntektHttpClientException(
                "Failed to run minsteinntekt. Response message ${response.responseMessage}. Error message: ${result.error.message}")
            is Result.Success -> result.get()
        }
    }
}

class RegelApiMinsteinntektHttpClientException(
    override val message: String
) : RuntimeException(message)