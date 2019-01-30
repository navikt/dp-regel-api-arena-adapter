package no.nav.dagpenger.regel.api.arena.adapter

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonDataException
import io.ktor.application.Application
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
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.alpha.BadRequestException
import no.nav.dagpenger.regel.api.arena.adapter.alpha.grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.alpha.minsteinntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.DagpengegrunnlagApi
import no.nav.dagpenger.regel.api.arena.adapter.v1.MinsteinntektApi
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

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        moshi(moshiInstance)
    }
    install(StatusPages) {
        exception<BadRequestException> { cause ->
            LOGGER.warn("Bad request") { cause }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<JsonDataException> { cause ->
            LOGGER.warn("Bad request") { cause }
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    routing {
        route("/v1") {
            MinsteinntektApi()
            DagpengegrunnlagApi()
        }
        minsteinntekt()
        grunnlag()
        naischecks()
    }
}

class RegelApiArenaAdapterException(override val message: String) : RuntimeException(message)