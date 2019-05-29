package no.nav.dagpenger.regel.api.arena.adapter.v2

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.internalV2.BehovRequest
import no.nav.dagpenger.regel.api.internalV2.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internalV2.extractGrunnlagOgSats
import no.nav.dagpenger.regel.api.internalV2.models.InntektsPeriode

fun Route.GrunnlagOgSatsApiV2(
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
        antallBarn = parametere.antallBarn,
        bruktInntektsPeriode = parametere.bruktInntektsPeriode?.let {
            InntektsPeriode(
                it.foersteMaaned,
                it.sisteMaaned
            )
        }
    )
}