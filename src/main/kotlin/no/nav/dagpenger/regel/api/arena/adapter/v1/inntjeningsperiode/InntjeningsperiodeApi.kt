package no.nav.dagpenger.regel.api.arena.adapter.v1.inntjeningsperiode

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.InntjeningsperiodeApi() {

    route("/inntjeningsperiode") {
        post {
            val parametere = call.receive<InntjeningsperiodeParametre>()

            val resultat = InntjeningsperiodeResultat(
                true,
                parametere
            )
            call.respond(HttpStatusCode.OK, resultat)
        }
    }
}