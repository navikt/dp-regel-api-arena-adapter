package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.TaskResponse

class RegelApiGrunnlagHttpClient(private val regelApiUrl: String) {

    fun startGrunnlagSubsumsjon(payload: GrunnlagOgSatsParametere): String {
        val url = "$regelApiUrl/grunnlag"

        val jsonAdapter = moshiInstance.adapter(GrunnlagOgSatsParametere::class.java)
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
            is Result.Failure -> throw RegelApiTasksHttpClient.RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success ->
                response.headers["Location"]?.first() ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    fun getGrunnlag(ressursUrl: String): GrunnlagSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(GrunnlagSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiTasksHttpClient.RegelApiHttpClientException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success -> result.get()
        }
    }
}