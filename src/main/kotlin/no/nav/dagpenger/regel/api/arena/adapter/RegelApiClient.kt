package no.nav.dagpenger.regel.api.arena.adapter

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson

class RegelApiClient(private val regelApiUrl: String) {

    enum class TaskStatus {
        PENDING, DONE
    }

    data class TaskResponse(
        val regel: Regel,
        val status: TaskStatus,
        val expires: String
    )

    data class TaskPollResponse(
        val task: TaskResponse?,
        val location: String?
    )

    fun startMinsteinntktBeregning(request: MinsteinntektBeregningsRequest): String {
        val url = "$regelApiUrl/minsteinntekt"
        val json = Gson().toJson(request).toString()
        val (_, response, result) =
            with(url.httpPost()
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)) {
                responseObject<TaskResponse>()
        }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                    response.statusCode, response.responseMessage, result.getException())
            is Result.Success ->
                response.headers["Location"]?.first() ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    fun getMinsteinntekt(ressursUrl: String): MinsteinntektBeregningsResponse {
        val url = "$regelApiUrl$ressursUrl"
        val (_, response, result) =
                with(url.httpGet()) { responseObject<MinsteinntektBeregningsResponse>() }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                    response.statusCode, response.responseMessage, result.getException())
            is Result.Success -> result.get()
        }
    }

    fun startGrunnlagBeregning(request: DagpengegrunnlagBeregningsRequest): String {
        val url = "$regelApiUrl/dagpengegrunnlag"
        val json = Gson().toJson(request).toString()
        val (_, response, result) =
                with(url.httpPost()
                        .header(mapOf("Content-Type" to "application/json"))
                        .body(json)) {
                    responseObject<TaskResponse>()
                }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                    response.statusCode, response.responseMessage, result.getException())
            is Result.Success ->
                response.headers["Location"]?.first() ?: throw RegelApiArenaAdapterException("No location")
        }
    }

    fun getGrunnlag(ressursUrl: String): DagpengegrunnlagBeregningsResponse {
        val url = "$regelApiUrl$ressursUrl"
        val (_, response, result) =
                with(url.httpGet()) { responseObject<DagpengegrunnlagBeregningsResponse>() }
        return when (result) {
            is Result.Failure -> throw RegelApiException(
                    response.statusCode, response.responseMessage, result.getException())
            is Result.Success -> result.get()
        }
    }

    fun pollTask (taskUrl: String): TaskPollResponse {
        val url = "$regelApiUrl$taskUrl"

        val (_, response, result) =
                with(url.httpGet().allowRedirects(false)) { responseObject<TaskResponse>() }

        return try {
            TaskPollResponse(result.get(), null)
        } catch (exception: Exception) {
            if (response.statusCode == 303) {
                return TaskPollResponse(null, response.headers["Location"]?.first())
            } else {
                throw RegelApiException(response.statusCode, response.responseMessage, exception)
            }
        }
    }

    class RegelApiException(val statusCode: Int, override val message: String, override val cause: Throwable) : RuntimeException(message, cause)
}