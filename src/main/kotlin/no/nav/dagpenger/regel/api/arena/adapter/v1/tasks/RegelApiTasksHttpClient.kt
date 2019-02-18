package no.nav.dagpenger.regel.api.arena.adapter.v1.tasks

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.responseObject
import java.lang.Thread.sleep
import java.time.Instant

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
                throw RegelApiTasksHttpClientException(
                    response.responseMessage, exception
                )
            }
        }
    }

    fun pollTaskUntilDone(taskUrl: String, timeoutSeconds: Int = 10): TaskPollResponse {
        val runUntilTime = Instant.now().toEpochMilli() + timeoutSeconds * 1000

        var taskResponse = pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            if (Instant.now().toEpochMilli() > runUntilTime) {
                throw RegelApiTimeoutException("Polled task status for more than $timeoutSeconds seconds")
            }

            sleep(50)
            taskResponse = pollTask(taskUrl)
        }
        return taskResponse
    }
}

class RegelApiTasksHttpClientException(
    override val message: String,
    override val cause: Throwable
) : RuntimeException(message, cause)

class RegelApiTimeoutException(override val message: String) : RuntimeException(message)