package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.core.Method
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import java.time.Duration

private val LOGGER = KotlinLogging.logger {}

internal class RegelApiStatusHttpClient(
    private val client: FuelHttpClient,
    private val timeout: Duration = Duration.ofSeconds(20)
) {
    private val delayDuration = Duration.ofMillis(100)

    private fun pollInternal(statusUrl: String): BehovStatusPollResult {

        val timer = clientLatencyStats.labels("poll").startTimer()
        try {
            val (_, response, result) = client.request(Method.GET, statusUrl) {
                it.allowRedirects(false)
            }.response()

            return result.fold(
                success = {
                    when (response.statusCode) {
                        303 -> BehovStatusPollResult(pending = false, location = response.headers["Location"].first())
                        else -> {
                            LOGGER.info("Polling: $response")
                            BehovStatusPollResult(pending = true, location = null)
                        }
                    }
                },
                failure = {
                    LOGGER.error("Failed polling $statusUrl")
                    throw RegelApiStatusHttpClientException(
                        response.responseMessage + "Status code: ${response.statusCode}",
                        it.exception
                    )
                }
            )
        } finally {
            timer.observeDuration()
        }
    }

    suspend fun pollStatus(statusUrl: String): String {
        try {
            return withTimeout(timeout.toMillis()) {
                return@withTimeout pollWithDelay(statusUrl)
            }
        } catch (e: Exception) {
            when (e) {
                is TimeoutCancellationException -> throw RegelApiTimeoutException("Polled behov status for more than ${timeout.toMillis()} milliseconds")
                else -> throw RegelApiStatusHttpClientException("Failed", e)
            }
        }
    }

    private suspend fun pollWithDelay(statusUrl: String): String {
        val status = pollInternal(statusUrl)
        return if (status.isPending()) {
            delay(delayDuration)
            pollWithDelay(statusUrl)
        } else {
            status.location ?: throw RegelApiArenaAdapterException("Did not get location with task")
        }
    }
}

private data class BehovStatusPollResult(
    val pending: Boolean,
    val location: String?
) {
    fun isPending() = pending
}

class RegelApiStatusHttpClientException(
    override val message: String,
    override val cause: Throwable
) : RuntimeException(message, cause)

class RegelApiTimeoutException(override val message: String) : RuntimeException(message)
