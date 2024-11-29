package no.nav.dagpenger.regel.api.internal

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat
import no.nav.dagpenger.regel.api.serder.jacksonObjectMapper
import java.time.Duration

internal class InntektApiInntjeningsperiodeHttpClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String,
    httpClientEngine: HttpClientEngine = CIO.create { requestTimeout = Long.MAX_VALUE },
) {
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

    suspend fun getInntjeningsperiode(parametere: InntjeningsperiodeParametre): InntjeningsperiodeResultat {
        val url = "/v1/samme-inntjeningsperiode"

        try {
            return httpClient
                .post(url) {
                    header("Content-Type", "application/json")
                    setBody(
                        parametere,
                    )
                }.body<InntjeningsPeriodeRespond>()
                .let {
                    InntjeningsperiodeResultat(it.sammeInntjeningsPeriode, parametere)
                }
        } catch (e: Exception) {
            throw InntektApiInntjeningsperiodeHttpClientException(
                "Failed to return samme-inntjeningsperiode. ",
            )
        }
    }

    private data class InntjeningsPeriodeRequest(
        val beregningsdato: String,
        val inntektsId: String,
    )

    private data class InntjeningsPeriodeRespond(
        val sammeInntjeningsPeriode: Boolean,
    )
}

class InntektApiInntjeningsperiodeHttpClientException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
