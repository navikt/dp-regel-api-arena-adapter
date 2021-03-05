package no.nav.dagpenger.regel.api.internal

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withTimeout
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.internal.models.BehovStatus
import no.nav.dagpenger.regel.api.internal.models.BehovStatusResponse
import java.time.Duration

internal class RegelApiStatusHttpClient(
    private val client: FuelHttpClient,
    private val timeout: Duration = Duration.ofSeconds(20)
) {
    private val delayDuration = Duration.ofMillis(100)

    private fun pollInternal(statusUrl: String): BehovStatusPollResult {

        val timer = clientLatencyStats.labels("poll").startTimer()
        try {
            val (_, response, result) = client.get<BehovStatusResponse>(statusUrl) { request ->
                request.allowRedirects(false)
            }

            return try {
                BehovStatusPollResult(result.get().status, null)
            } catch (exception: Exception) {
                if (response.statusCode == 303) {
                    return BehovStatusPollResult(
                        null,
                        response.headers["Location"].first()
                    )
                } else {
                    throw RegelApiStatusHttpClientException(
                        response.responseMessage + "Status code: ${response.statusCode}",
                        exception
                    )
                }
            }
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
    val status: BehovStatus?,
    val location: String?
) {
    fun isPending() = status == BehovStatus.PENDING
}

class RegelApiStatusHttpClientException(
    override val message: String,
    override val cause: Throwable
) : RuntimeException(message, cause)

class RegelApiTimeoutException(override val message: String) : RuntimeException(message)
