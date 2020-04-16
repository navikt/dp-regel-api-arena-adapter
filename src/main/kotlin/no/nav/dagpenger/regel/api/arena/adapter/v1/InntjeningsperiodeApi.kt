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
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient

fun Route.InntjeningsperiodeApi(inntektApiberegningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient) {

    route("/inntjeningsperiode") {
        post {
            val parametere = call.receive<InntjeningsperiodeParametre>()

            val parametereInternal = no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre(
                parametere.aktorId,
                parametere.vedtakId,
                parametere.beregningsdato,
                parametere.inntektsId
            )
            val resultatInternal = inntektApiberegningsdatoHttpClient.getInntjeningsperiode(parametereInternal)

            val resultat = InntjeningsperiodeResultat(
                resultatInternal.sammeInntjeningsPeriode,
                InntjeningsperiodeParametre(
                    resultatInternal.parametere.aktorId,
                    resultatInternal.parametere.vedtakId,
                    resultatInternal.parametere.beregningsdato,
                    resultatInternal.parametere.inntektsId
                ))

            call.respond(HttpStatusCode.OK, resultat)
        }
    }
}
