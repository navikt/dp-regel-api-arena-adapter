package no.nav.dagpenger.regel.api.arena.adapter

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.v1.GrunnlagOgSatsApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.InntjeningsperiodeApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.InvalidInnteksperiodeException
import no.nav.dagpenger.regel.api.arena.adapter.v1.MinsteinntektOgPeriodeApi
import no.nav.dagpenger.regel.api.internal.RegelApiTasksHttpClient
import no.nav.dagpenger.regel.api.internal.RegelApiTimeoutException
import no.nav.dagpenger.regel.api.internal.grunnlag.RegelApiGrunnlagHttpClient
import no.nav.dagpenger.regel.api.internal.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.internal.inntjeningsperiode.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.minsteinntekt.RegelApiMinsteinntektHttpClient
import no.nav.dagpenger.regel.api.internal.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.internal.periode.RegelApiPeriodeHttpClient
import no.nav.dagpenger.regel.api.internal.periode.SynchronousPeriode
import no.nav.dagpenger.regel.api.internal.sats.RegelApiSatsHttpClient
import no.nav.dagpenger.regel.api.internal.sats.SynchronousSats
import org.slf4j.event.Level
import java.net.URI
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

fun main() {
    val env = Environment()

    val regelApiTasksHttpClient =
        RegelApiTasksHttpClient(env.dpRegelApiUrl)
    val regelApiMinsteinntektHttpClient = RegelApiMinsteinntektHttpClient(env.dpRegelApiUrl)
    val regelApiPeriodeHttpClient = RegelApiPeriodeHttpClient(env.dpRegelApiUrl)
    val regelApiGrunnlagHttpClient = RegelApiGrunnlagHttpClient(env.dpRegelApiUrl)
    val regelApiSatsHttpClient = RegelApiSatsHttpClient(env.dpRegelApiUrl)

    val synchronousMinsteinntekt = SynchronousMinsteinntekt(regelApiMinsteinntektHttpClient, regelApiTasksHttpClient)
    val synchronousPeriode = SynchronousPeriode(regelApiPeriodeHttpClient, regelApiTasksHttpClient)
    val synchronousGrunnlag = SynchronousGrunnlag(regelApiGrunnlagHttpClient, regelApiTasksHttpClient)
    val synchronousSats = SynchronousSats(regelApiSatsHttpClient, regelApiTasksHttpClient)

    val inntektApiBeregningsdatoHttpClient = InntektApiInntjeningsperiodeHttpClient(env.inntektApiUrl)

    val app = embeddedServer(Netty, port = env.httpPort) {
        regelApiAdapter(
            synchronousMinsteinntekt,
            synchronousPeriode,
            synchronousGrunnlag,
            synchronousSats,
            inntektApiBeregningsdatoHttpClient
        )
    }

    app.start(wait = false)
    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.regelApiAdapter(
    synchronousMinsteinntekt: SynchronousMinsteinntekt,
    synchronousPeriode: SynchronousPeriode,
    synchronousGrunnlag: SynchronousGrunnlag,
    synchronousSats: SynchronousSats,
    inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient
) {

    install(DefaultHeaders)
    install(CallLogging) {
        level = Level.INFO

        filter { call ->
            !call.request.path().startsWith("/isAlive") &&
                !call.request.path().startsWith("/isReady") &&
                !call.request.path().startsWith("/metrics")
        }
    }
    install(ContentNegotiation) {
        moshi(moshiInstance)
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
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, problem)
        }
        exception<JsonEncodingException> { cause ->
            LOGGER.warn(cause.message, cause)
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = "Parameteret er ikke gyldig json",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, problem)
        }
        exception<InvalidInnteksperiodeException> { cause ->
            LOGGER.warn(cause.message)
            val problem = Problem(
                type = URI.create("urn:dp:error:parameter"),
                title = cause.message,
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, problem)
        }
        exception<RegelApiTimeoutException> { cause ->
            LOGGER.error("Tidsavbrudd ved beregning av regel", cause)
            val problem = Problem(
                type = URI.create("urn:dp:error:regelberegning:tidsavbrudd"),
                title = "Tidsavbrudd ved beregning av regel",
                detail = cause.message,
                status = 502
            )
            call.respond(HttpStatusCode.GatewayTimeout, problem)
        }
    }

    routing {
        route("/v1") {
            MinsteinntektOgPeriodeApi(synchronousMinsteinntekt, synchronousPeriode)
            GrunnlagOgSatsApi(synchronousGrunnlag, synchronousSats)
            InntjeningsperiodeApi(inntektApiBeregningsdatoHttpClient)
        }
        naischecks()
    }
}

class RegelApiArenaAdapterException(override val message: String) : RuntimeException(message)
