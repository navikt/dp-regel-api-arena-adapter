package no.nav.dagpenger.regel.api.arena.adapter.v1.tasks

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.responseObject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withTimeout
import java.time.Duration

class RegelApiTasksHttpClient(private val regelApiUrl: String, private val timeout: Duration = Duration.ofSeconds(10)) {
    private val delayDuration = Duration.ofMillis(100)

    private fun pollInternal(taskUrl: String): TaskPollResponse {
        val url = "$regelApiUrl$taskUrl"

        val (_, response, result) =
            with(url.httpGet().allowRedirects(false)) { responseObject<TaskResponse>() }

        return try {
            TaskPollResponse(result.get(), null)
        } catch (exception: Exception) {
            if (response.statusCode == 303) {
                return TaskPollResponse(
                    null,
                    response.headers["Location"].first()
                )
            } else {
                throw RegelApiTasksHttpClientException(
                    response.responseMessage, exception
                )
            }
        }
    }

    suspend fun pollTask(taskUrl: String): TaskPollResponse {
        try {
            return withTimeout(timeout.toMillis()) {
                return@withTimeout pollWithDelay(taskUrl)
            }
        } catch (e: Exception) {
            when (e) {
                is TimeoutCancellationException -> throw RegelApiTimeoutException("Polled task status for more than ${timeout.toMillis()} milliseconds")
                else -> throw RegelApiTasksHttpClientException("Failed", e)
            }
        }
    }

    private suspend fun pollWithDelay(taskUrl: String): TaskPollResponse {
        val task = pollInternal(taskUrl)
        return if (task.isDone()) {
            task
        } else {
            delay(delayDuration)
            pollWithDelay(taskUrl)
        }
    }
}

class RegelApiTasksHttpClientException(
    override val message: String,
    override val cause: Throwable
) : RuntimeException(message, cause)

class RegelApiTimeoutException(override val message: String) : RuntimeException(message)