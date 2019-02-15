package no.nav.dagpenger.regel.api.arena.adapter

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.GrunnlagOgSatsApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.RegelApiGrunnlagHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.MinsteinntektOgPeriodeApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.RegelApiMinsteinntektHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.RegelApiPeriodeHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.SynchronousPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.RegelApiSatsHttpClient
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SynchronousSats
import no.nav.dagpenger.regel.api.arena.adapter.v1.tasks.RegelApiTasksHttpClient
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

fun main() {
    val env = Environment()

    val app = embeddedServer(Netty, port = env.httpPort) {
        regelApiAdapter()
    }

    app.start(wait = false)
    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.regelApiAdapter() {
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

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        moshi(moshiInstance)
    }
    install(StatusPages) {
        exception<JsonDataException> { cause ->
            badRequest(cause)
        }
        exception<JsonEncodingException> { cause ->
            badRequest(cause)
        }
        routing {
            route("/v1") {
                MinsteinntektOgPeriodeApi(synchronousMinsteinntekt, synchronousPeriode)
                GrunnlagOgSatsApi(synchronousGrunnlag, synchronousSats)
            }
            naischecks()
        }
    }
}

private suspend fun <T : Throwable> PipelineContext<Unit, ApplicationCall>.badRequest(
    cause: T
) {
    call.respond(HttpStatusCode.BadRequest)
    throw cause
}

class RegelApiArenaAdapterException(override val message: String) : RuntimeException(message)
