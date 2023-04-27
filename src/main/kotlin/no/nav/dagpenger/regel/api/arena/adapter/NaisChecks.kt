package no.nav.dagpenger.regel.api.arena.adapter

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.naischecks() {
    get("/isAlive") {
        call.respondText("ALIVE", ContentType.Text.Plain)
    }
    get("/isReady") {
        call.respondText("READY", ContentType.Text.Plain)
    }
}
