package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.responseObject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withTimeout
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.internal.models.BehovStatus
import no.nav.dagpenger.regel.api.internal.models.BehovStatusResponse
import java.time.Duration

class RegelApiStatusHttpClient(private val regelApiUrl: String, private val timeout: Duration = Duration.ofSeconds(20)) {
    private val delayDuration = Duration.ofMillis(100)

    private fun pollInternal(statusUrl: String): BehovStatusPollResult {
        val (_, response, result) =
            with(statusUrl.httpGet().allowRedirects(false)) { responseObject<BehovStatusResponse>() }

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
                        response.responseMessage, exception
                )
            }
        }
    }

    suspend fun pollStatus(statusUrl: String): String {
        val url = "$regelApiUrl$statusUrl"
        try {
            return withTimeout(timeout.toMillis()) {
                return@withTimeout pollWithDelay(url)
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