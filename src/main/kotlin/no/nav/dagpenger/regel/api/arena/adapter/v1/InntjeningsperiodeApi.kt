package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntjeningsperiodeResultat
import no.nav.dagpenger.regel.api.internal.inntjeningsperiode.InntektApiBeregningsdatoHttpClient

fun Route.InntjeningsperiodeApi(inntektApiberegningsdatoHttpClient: InntektApiBeregningsdatoHttpClient) {

    route("/inntjeningsperiode") {
        post {
            val parametere = call.receive<InntjeningsperiodeParametre>()

            val gammelBeregningsdato = inntektApiberegningsdatoHttpClient.getBeregningsdato(parametere.inntektsId)

            val resultat = InntjeningsperiodeResultat(
                true,
                parametere
            )
            call.respond(HttpStatusCode.OK, resultat)
        }
    }
}