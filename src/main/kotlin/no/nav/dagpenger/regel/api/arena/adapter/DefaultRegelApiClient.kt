package no.nav.dagpenger.regel.api.arena.adapter

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import java.net.URI

class DefaultRegelApiClient(private val regelApiUrl: String) : RegelApiClient {

    override fun startMinsteinntktBeregning(request: MinsteinntektBeregningsRequest): URI {
        val url = "$regelApiUrl/minsteinntekt"
        val json = Gson().toJson(request).toString()
        val (_, response, result) =
            with(
                url.httpPost()
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)
            ) {
                responseObject<TaskResponse>()
            }

        return when (result) {
            is Result.Failure -> throw RegelApiException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success ->
                URI.create(response.headers["Location"]?.first()) ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    override fun getMinsteinntekt(ressursUrl: URI): MinsteinntektBeregningsResponse {
        val url = "$regelApiUrl$ressursUrl"
        val (_, response, result) =
            with(url.httpGet()) { responseObject<MinsteinntektBeregningsResponse>() }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success -> result.get()
        }
    }

    override fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): URI {
        val url = "$regelApiUrl/dagpengegrunnlag"
        val json = Gson().toJson(request).toString()
        val (_, response, result) =
            with(
                url.httpPost()
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)
            ) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success ->
                URI.create(response.headers["Location"]?.first()) ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    override fun getGrunnlag(ressursUrl: URI): DagpengegrunnlagBeregningsResponse {
        val url = "$regelApiUrl$ressursUrl"
        val (_, response, result) =
            with(url.httpGet()) { responseObject<DagpengegrunnlagBeregningsResponse>() }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                response.statusCode, response.responseMessage, result.getException()
            )
            is Result.Success -> result.get()
        }
    }

    override fun pollTask(taskUrl: URI): TaskPollResponse {
        val url = "$regelApiUrl$taskUrl"

        val (_, response, result) =
            with(url.httpGet().allowRedirects(false)) { responseObject<TaskResponse>() }

        return try {
            TaskPollResponse(result.get(), null)
        } catch (exception: Exception) {
            if (response.statusCode == 303) {
                return TaskPollResponse(null, URI.create(response.headers["Location"]?.first()))
            } else {
                throw RegelApiException(response.statusCode, response.responseMessage, exception)
            }
        }
    }
}