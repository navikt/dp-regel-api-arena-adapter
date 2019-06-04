package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internal.extractGrunnlagOgSats

fun Route.GrunnlagOgSatsApi(
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient
) {

    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()

            val behovRequest = behovFromParametere(parametere)

            val grunnlagOgSatsSubsumsjon =
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, ::extractGrunnlagOgSats)

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }
}

fun behovFromParametere(parametere: GrunnlagOgSatsParametere): BehovRequest {
    return BehovRequest(
            parametere.aktorId,
            parametere.vedtakId,
            parametere.beregningsdato,
            harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
            manueltGrunnlag = parametere.grunnlag,
            antallBarn = parametere.antallBarn
    )
}