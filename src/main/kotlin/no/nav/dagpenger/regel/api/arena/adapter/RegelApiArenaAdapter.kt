package no.nav.dagpenger.regel.api.arena.adapter

import de.nielsfalk.ktor.swagger.SwaggerSupport
import de.nielsfalk.ktor.swagger.version.shared.Information
import de.nielsfalk.ktor.swagger.version.v2.Swagger
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.locations.Locations
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
    val app = embeddedServer(Netty, port = 8093, module = Application::regelApiAdapter)
    app.start(wait = false)
    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.regelApiAdapter() {

    val regelApiClient = RegelApiClient("http://localhost:8092")

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Locations)

    install(SwaggerSupport) {
        forwardRoot = true
        val information = Information(
                title = "Dagpenger regel-api arena-adapter"
        )
        swagger = Swagger().apply {
            info = information
        }
    }

    routing {
        minsteinntekt(regelApiClient)
        grunnlag(regelApiClient)
    }
}

class RegelApiArenaAdapterException(override val message: String) : RuntimeException(message)