package no.nav.dagpenger.regel.api.internal

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withTimeout
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import no.nav.dagpenger.regel.api.serder.jacksonObjectMapper
import java.time.Duration
import java.time.LocalDate

internal class RegelApiBehovHttpClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String,
    httpClientEngine: HttpClientEngine = CIO.create { requestTimeout = Long.MAX_VALUE },
    private val timeout: Duration = Duration.ofSeconds(20),
) : RegelApi {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.RegelApiBehovHttpClient")
    }

    private val httpClient =
        HttpClient(httpClientEngine) {
            expectSuccess = true
            followRedirects = false
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(jacksonObjectMapper))
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                connectTimeoutMillis = Duration.ofSeconds(15).toMillis()
                requestTimeoutMillis = Duration.ofSeconds(15).toMillis()
                socketTimeoutMillis = Duration.ofSeconds(15).toMillis()
            }
            defaultRequest {
                url(baseUrl)
                header(HttpHeaders.Authorization, "Bearer ${tokenProvider.invoke()}")
            }
        }

    private val delayDuration = Duration.ofMillis(100)

    override suspend fun run(behovRequest: BehovRequest): String =
        try {
            val response =
                httpClient.post("/behov") {
                    header("Content-Type", "application/json")
                    setBody(behovRequest)
                }
            response.headers["Location"]
                ?: throw RegelApiBehovHttpClientException("Fant ikke Location header i response")
        } catch (e: Exception) {
            throw RegelApiBehovHttpClientException(
                "Failed to run behov",
                e,
            )
        }

    override suspend fun kreverNyVurdering(
        subsumsjonIder: List<String>,
        beregningsdato: LocalDate,
    ): Boolean {
        try {
            return httpClient
                .post("/lovverk/vurdering/minsteinntekt") {
                    header("Content-Type", "application/json")
                    setBody(KreverNyVurderingParametre(beregningsdato, subsumsjonIder))
                }.body<KreverNyVurderingRespons>()
                .nyVurdering
        } catch (e: Exception) {
            throw RegelApiMinsteinntektNyVurderingException(
                "Failed to check reberegning.",
                e,
            )
        }
    }

    override suspend fun pollStatus(statusUrl: String): String {
        try {
            return withTimeout(timeout.toMillis()) {
                return@withTimeout pollWithDelay(statusUrl)
            }
        } catch (e: Exception) {
            val behovId = statusUrl.substringAfterLast("/")
            when (e) {
                is TimeoutCancellationException -> throw RegelApiTimeoutException(
                    "Polled behov status (behovId=$behovId) for more than ${timeout.toMillis()} milliseconds",
                )

                else -> throw RegelApiStatusHttpClientException("Failed", e)
            }
        }
    }

    override suspend fun getSubsumsjon(subsumsjonLocation: String): Subsumsjon {
        try {
            return httpClient.get(subsumsjonLocation).body<Subsumsjon>().also {
                withLoggingContext(
                    "requestId" to it.requestId.toString(),
                    "behovId" to it.behovId,
                    "aktorId" to it.faktum.aktorId,
                    "vedtakId" to it.faktum.regelkontekst.id,
                    "inntektsId" to it.faktum.inntektsId.toString(),
                ) {
                    sikkerlogg.info { "Fikk svar på subsumsjon: $it" }
                }
            }
        } catch (e: Exception) {
            throw RegelApiSubsumsjonHttpClientException(
                "Failed to fetch subsumsjon",
                e,
            )
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

    private suspend fun pollInternal(statusUrl: String): BehovStatusPollResult {
        val timer = clientLatencyStats.labels("poll").startTimer()

        try {
            val response =
                httpClient.get(statusUrl) {
                    header("Accept", "application/json")
                }
            return when (response.status.value) {
                303 -> BehovStatusPollResult(pending = false, location = response.headers["Location"])
                else -> {
                    LOGGER.debug { "Polling: $response" }
                    BehovStatusPollResult(pending = true, location = null)
                }
            }
        } catch (e: RedirectResponseException) {
            return BehovStatusPollResult(pending = false, location = e.response.headers["Location"])
        } catch (e: Exception) {
            LOGGER.error { "Failed polling $statusUrl" }
            throw RegelApiStatusHttpClientException(
                "Failed to poll behov status",
                e,
            )
        } finally {
            timer.observeDuration()
        }
    }
}

private data class BehovStatusPollResult(
    val pending: Boolean,
    val location: String?,
) {
    fun isPending() = pending
}
