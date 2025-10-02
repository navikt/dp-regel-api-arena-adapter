package no.nav.dagpenger.regel.api.arena.adapter

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.JsonMappingException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.regel.api.Configuration
import no.nav.dagpenger.regel.api.arena.adapter.v1.InvalidInnteksperiodeException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NegativtGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NullGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.SubsumsjonProblem
import no.nav.dagpenger.regel.api.arena.adapter.v1.UgyldigParameterkombinasjonException
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlagOgSatsApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.inntjeningsperiodeApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntektOgPeriodeApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.IllegalInntektIdException
import no.nav.dagpenger.regel.api.arena.adapter.v1.nyVurderingApi
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApi
import no.nav.dagpenger.regel.api.internal.RegelApiBehovHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiMinsteinntektNyVurderingException
import no.nav.dagpenger.regel.api.internal.RegelApiTimeoutException
import no.nav.dagpenger.regel.api.serder.jacksonObjectMapper
import org.slf4j.event.Level
import java.net.URI
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

fun main() {
    val config = Configuration()

    val jwkProvider =
        JwkProviderBuilder(URI(config.application.jwksUrl).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    val inntektApiBeregningsdatoHttpClient =
        InntektApiInntjeningsperiodeHttpClient(
            baseUrl = config.application.dpInntektApiUrl,
            tokenProvider = config.tokenProvider,
        )

    val behovHttpClient =
        RegelApiBehovHttpClient(
            baseUrl = config.application.dpRegelApiBaseUrl,
            tokenProvider = config.tokenProvider,
        )

    val app =
        embeddedServer(Netty, port = config.application.httpPort) {
            regelApiAdapter(
                config.application.jwksIssuer,
                jwkProvider,
                inntektApiBeregningsdatoHttpClient,
                behovHttpClient,
                config.application.optionalJwt,
            )
        }

    app.start(wait = true)
}

internal fun Application.regelApiAdapter(
    jwtIssuer: String,
    jwkProvider: JwkProvider,
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient,
    regelApi: RegelApi,
    optionalJwt: Boolean = false,
    collectorRegistry: PrometheusRegistry = PrometheusRegistry.defaultRegistry,
) {
    install(DefaultHeaders)
    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()

        filter { call ->
            !call.request.path().startsWith("/isAlive") &&
                !call.request.path().startsWith("/isReady") &&
                !call.request.path().startsWith("/metrics")
        }
    }
    install(Authentication) {
        jwt {
            verifier(jwkProvider, jwtIssuer)
            realm = "dp-regel-api-arena-adapter"
            validate { credentials ->
                JWTPrincipal(credentials.payload)
            }
        }
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(jacksonObjectMapper))
    }

    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            LOGGER.error(cause) { "Unhåndtert feil ved beregning av regel" }
            val problem =
                Problem(
                    title = "Uhåndtert feil",
                    detail = cause.message,
                )
            call.respond(HttpStatusCode.InternalServerError, problem)
        }
        exception<JsonMappingException> { call, cause ->
            LOGGER.warn(cause) { cause.message }
            val status = HttpStatusCode.BadRequest
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:parameter"),
                    title = "Parameteret er ikke gyldig json. '${cause.message}'",
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<BadRequestException> { call, cause ->
            LOGGER.warn(cause) { cause.message }
            val status = HttpStatusCode.BadRequest
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:parameter"),
                    title = "Parameteret er ikke gyldig json",
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<InvalidInnteksperiodeException> { call, cause ->
            LOGGER.warn { cause.message }
            val status = HttpStatusCode.BadRequest
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:parameter"),
                    title = cause.message,
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<IllegalInntektIdException> { call, cause ->
            LOGGER.warn(cause) { cause.message }
            val status = HttpStatusCode.BadRequest
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:parameter"),
                    title = "InnteksId er ikke gyldig",
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<RegelApiTimeoutException> { call, cause ->
            LOGGER.error(cause) { "Tidsavbrudd ved beregning av regel" }
            val status = HttpStatusCode.GatewayTimeout
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:regelberegning:tidsavbrudd"),
                    title = "Tidsavbrudd ved beregning av regel",
                    detail = cause.message,
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<SubsumsjonProblem> { call, cause ->
            call.respond(HttpStatusCode.BadGateway, cause.problem).also {
                LOGGER.error(cause) { "Problem ved beregning av subsumsjon" }
            }
        }
        exception<RegelApiMinsteinntektNyVurderingException> { call, cause ->
            LOGGER.error(cause) { "Kan ikke fastslå om minsteinntekt må revurderes" }
            val status = HttpStatusCode.InternalServerError
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:revurdering:minsteinntekt"),
                    title = "Feil ved sjekk om minsteinntekt må revurderes",
                    detail = cause.message,
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<NegativtGrunnlagException> { call, cause ->
            LOGGER.error(cause) { "Negativt grunnlag" }
            val status = HttpStatusCode.InternalServerError
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:regelberegning:grunnlag:negativ"),
                    title = "Grunnlag er negativt",
                    detail = cause.message,
                    status = status.value,
                )
            call.respond(status, problem)
        }

        exception<NullGrunnlagException> { call, cause ->
            LOGGER.warn(cause) { "0 grunnlag" }
            val status = HttpStatusCode.InternalServerError
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:regelberegning:grunnlag:0"),
                    title = "Grunnlag er 0",
                    detail = cause.message,
                    status = status.value,
                )
            call.respond(status, problem)
        }
        exception<UgyldigParameterkombinasjonException> { call, cause ->
            LOGGER.warn(cause) { cause.message }
            val status = HttpStatusCode.BadRequest
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:parameter"),
                    title = "Ugyldig kombinasjon av parametere: ${cause.message}",
                    status = status.value,
                )
            call.respond(status, problem)
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            val status = HttpStatusCode.Unauthorized
            LOGGER.warn { "Unauthorized call" }
            val problem =
                Problem(
                    type = URI.create("urn:dp:error:uautorisert"),
                    title = "Uautorisert",
                    status = status.value,
                )
            call.respond(status, problem)
        }
    }

    routing {
        authenticate(optional = optionalJwt) {
            route("/v1") {
                grunnlagOgSatsApi(regelApi)
                minsteinntektOgPeriodeApi(regelApi)
                inntjeningsperiodeApi(inntektApiBeregningsdatoHttpClient)
                nyVurderingApi(regelApi)
            }
        }

        naischecks()
        metrics()
    }
}

class RegelApiArenaAdapterException(
    override val message: String,
) : RuntimeException(message)
