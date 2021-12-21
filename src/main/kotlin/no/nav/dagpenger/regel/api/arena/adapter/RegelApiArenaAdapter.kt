package no.nav.dagpenger.regel.api.arena.adapter

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.Configuration
import no.nav.dagpenger.regel.api.arena.adapter.v1.GrunnlagOgSatsApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.InntjeningsperiodeApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.InvalidInnteksperiodeException
import no.nav.dagpenger.regel.api.arena.adapter.v1.MinsteinntektOgPeriodeApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.NegativtGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NullGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NyVurderingApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.SubsumsjonProblem
import no.nav.dagpenger.regel.api.arena.adapter.v1.UgyldigParameterkombinasjonException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.IllegalInntektIdException
import no.nav.dagpenger.regel.api.internal.FuelHttpClient
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiBehovHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiMinsteinntektNyVurderingException
import no.nav.dagpenger.regel.api.internal.RegelApiNyVurderingHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiStatusHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiSubsumsjonHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiTimeoutException
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import org.slf4j.event.Level
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

fun main() {

    val config = Configuration()

    val jwkProvider = JwkProviderBuilder(URL(config.application.jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val inntektApiBeregningsdatoHttpClient = InntektApiInntjeningsperiodeHttpClient(FuelHttpClient(config.application.dpInntektApiUrl))

    val regelApiHttpClient = FuelHttpClient(config.application.dpRegelApiUrl, config.auth.regelApiKey)

    val behovHttpClient = RegelApiBehovHttpClient(regelApiHttpClient)
    val statusHttpClient = RegelApiStatusHttpClient(regelApiHttpClient)
    val subsumsjonHttpClient = RegelApiSubsumsjonHttpClient(regelApiHttpClient)
    val regelApiNyVurderingHttpClient = RegelApiNyVurderingHttpClient(regelApiHttpClient)

    val synchronousSubsumsjonClient =
        SynchronousSubsumsjonClient(behovHttpClient, statusHttpClient, subsumsjonHttpClient)

    val app = embeddedServer(Netty, port = config.application.httpPort) {
        regelApiAdapter(
            config.application.jwksIssuer,
            jwkProvider,
            inntektApiBeregningsdatoHttpClient,
            synchronousSubsumsjonClient,
            regelApiNyVurderingHttpClient,
            config.application.optionalJwt
        )
    }

    app.start(wait = false)
    Runtime.getRuntime().addShutdownHook(
        Thread {
            app.stop(5000, 60000)
        }
    )
}

internal fun Application.regelApiAdapter(
    jwtIssuer: String,
    jwkProvider: JwkProvider,
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient,
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient,
    kreverRebergningClient: RegelApiNyVurderingHttpClient,
    optionalJwt: Boolean = false,
    collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
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
                log.info("'${credentials.payload.subject}' authenticated")
                JWTPrincipal(credentials.payload)
            }
        }
    }
    install(ContentNegotiation) {
        moshi(moshiInstance)
    }

    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM)
    }
    install(StatusPages) {
        exception<Throwable> { cause ->
            LOGGER.error("Unhåndtert feil ved beregning av regel", cause)
            val problem = Problem(
                title = "Uhåndtert feil",
                detail = cause.message
            )
            call.respond(HttpStatusCode.InternalServerError, problem)
        }
        exception<JsonDataException> { cause ->
            LOGGER.warn(cause.message, cause)
            val status = HttpStatusCode.BadRequest
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<JsonEncodingException> { cause ->
            LOGGER.warn(cause.message, cause)
            val status = HttpStatusCode.BadRequest
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = "Parameteret er ikke gyldig json",
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<InvalidInnteksperiodeException> { cause ->
            LOGGER.warn(cause.message)
            val status = HttpStatusCode.BadRequest
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = cause.message,
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<IllegalInntektIdException> { cause ->
            LOGGER.warn(cause.message)
            val status = HttpStatusCode.BadRequest
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = "InnteksId er ikke gyldig",
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<RegelApiTimeoutException> { cause ->
            LOGGER.error("Tidsavbrudd ved beregning av regel", cause)
            val status = HttpStatusCode.GatewayTimeout
            val problem = Problem(
                type = URI.create("urn:dp:error:regelberegning:tidsavbrudd"),
                title = "Tidsavbrudd ved beregning av regel",
                detail = cause.message,
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<SubsumsjonProblem> { cause ->
            call.respond(HttpStatusCode.BadGateway, cause.problem).also {
                LOGGER.error("Problem ved beregning av subsumsjon", cause)
            }
        }
        exception<RegelApiMinsteinntektNyVurderingException> { cause ->
            LOGGER.error("Kan ikke fastslå om minsteinntekt må revurderes", cause)
            val status = HttpStatusCode.InternalServerError
            val problem = Problem(
                type = URI.create("urn:dp:error:revurdering:minsteinntekt"),
                title = "Feil ved sjekk om minsteinntekt må revurderes",
                detail = cause.message,
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<NegativtGrunnlagException> { cause ->
            LOGGER.error("Negativt grunnlag", cause)
            val status = HttpStatusCode.InternalServerError
            val problem = Problem(
                type = URI.create("urn:dp:error:regelberegning:grunnlag:negativ"),
                title = "Grunnlag er negativt",
                detail = cause.message,
                status = status.value
            )
            call.respond(status, problem)
        }

        exception<NullGrunnlagException> { cause ->
            LOGGER.warn(cause) { "0 grunnlag" }
            val status = HttpStatusCode.InternalServerError
            val problem = Problem(
                type = URI.create("urn:dp:error:regelberegning:grunnlag:0"),
                title = "Grunnlag er 0",
                detail = cause.message,
                status = status.value
            )
            call.respond(status, problem)
        }
        exception<UgyldigParameterkombinasjonException> { cause ->
            LOGGER.warn(cause.message)
            val status = HttpStatusCode.BadRequest
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = "Ugyldig kombinasjon av parametere: ${cause.message}",
                status = status.value
            )
            call.respond(status, problem)
        }

        status(HttpStatusCode.Unauthorized) {
            val status = HttpStatusCode.Unauthorized
            LOGGER.warn("Unauthorized call")
            val problem = Problem(
                type = URI.create("urn:dp:error:uautorisert"),
                title = "Uautorisert",
                status = status.value
            )
            call.respond(status, problem)
        }
    }

    routing {
        authenticate(optional = optionalJwt) {
            route("/v1") {
                GrunnlagOgSatsApi(synchronousSubsumsjonClient)
                MinsteinntektOgPeriodeApi(synchronousSubsumsjonClient)
                InntjeningsperiodeApi(inntektApiBeregningsdatoHttpClient)
                NyVurderingApi(kreverRebergningClient)
            }
        }

        naischecks()
        metrics()
    }
}

class RegelApiArenaAdapterException(
    override
    val message: String
) : RuntimeException(message)
