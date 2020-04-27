package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internal.extractMinsteinntektOgPeriode
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode

fun Route.MinsteinntektOgPeriodeApi(
    synchronousSubsumsjonClient: SynchronousSubsumsjonClient
) {

    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektOgPeriodeParametere>()

            parametere.validate()

            val behovRequest = behovFromParametere(parametere)

            val minsteinntektOgPeriodeSubsumsjon: MinsteinntektOgPeriodeSubsumsjon =
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, ::extractMinsteinntektOgPeriode)

            call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
        }
    }
}

fun MinsteinntektOgPeriodeParametere.validate() {
    this.bruktInntektsPeriode?.let {
        if (it.foersteMaaned.isAfter(it.sisteMaaned)) throw InvalidInnteksperiodeException(
                "Feil bruktInntektsPeriode: foersteMaaned=${it.foersteMaaned} er etter sisteMaaned=${it.sisteMaaned}"
        )
    }
    if (this.oppfyllerKravTilLaerling && this.harAvtjentVerneplikt == true) throw UgyldigParameterkombinasjonException(
        "harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig"
    )
}

fun behovFromParametere(parametere: MinsteinntektOgPeriodeParametere): BehovRequest {
    return BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        lærling = parametere.oppfyllerKravTilLaerling,
        bruktInntektsPeriode = parametere.bruktInntektsPeriode?.let {
            InntektsPeriode(
                førsteMåned = it.foersteMaaned,
                sisteMåned = it.sisteMaaned
            )
        }
    )
}