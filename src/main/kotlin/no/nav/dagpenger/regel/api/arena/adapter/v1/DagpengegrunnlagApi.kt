package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.DagpengegrunnlagApi() {

    route("/dagpengegrunnlag") {
        post {
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
}