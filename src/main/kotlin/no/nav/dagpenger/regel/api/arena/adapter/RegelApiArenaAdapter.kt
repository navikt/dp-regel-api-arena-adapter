package no.nav.dagpenger.regel.api.arena.adapter

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

private val LOGGER = KotlinLogging.logger {}

enum class Regel {
    MINSTEINNTEKT, GRUNNLAG
}

fun main(args: Array<String>) {
    val env = Environment()
    val regelApiClientDummy = RegelApiDummy()

    val app = embeddedServer(Netty, port = env.httpPort) {
        regelApiAdapter(regelApiClientDummy)
    }

    app.start(wait = false)
    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.regelApiAdapter(regelApiClient: RegelApiClient) {

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        moshi {
            add(YearMonthJsonAdapter())
            add(LocalDateTimeJsonAdapter())
            add(LocalDateJsonAdapter())
            add(KotlinJsonAdapterFactory())
        }
    }
    install(StatusPages) {
        exception<BadRequestException> { cause ->
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    routing {
        minsteinntekt(regelApiClient)
        grunnlag(regelApiClient)
        naischecks()
    }
}

class RegelApiArenaAdapterException(override val message: String) : RuntimeException(message)