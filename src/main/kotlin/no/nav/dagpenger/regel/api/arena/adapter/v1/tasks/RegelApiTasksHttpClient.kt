package no.nav.dagpenger.regel.api.arena.adapter.v1.tasks

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.responseObject

class RegelApiTasksHttpClient(private val regelApiUrl: String) {

    fun pollTask (taskUrl: String): TaskPollResponse {
        val url = "$regelApiUrl$taskUrl"

        val (_, response, result) =
            with(url.httpGet().allowRedirects(false)) { responseObject<TaskResponse>() }

        return try {
            TaskPollResponse(result.get(), null)
        } catch (exception: Exception) {
            if (response.statusCode == 303) {
                return TaskPollResponse(
                    null,
                    response.headers["Location"]?.first()
                )
            } else {
                throw RegelApiHttpClientException(
                    response.statusCode,
                    response.responseMessage,
                    exception
                )
            }
        }
    }

    class RegelApiHttpClientException(val statusCode: Int, override val message: String, override val cause: Throwable) : RuntimeException(message, cause)
}