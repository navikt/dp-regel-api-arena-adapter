package no.nav.dagpenger.regel.api.arena.adapter.v2

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.MinsteinntektOgPeriodeParametere
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

            validateParameters(parametere)

            val behovRequest = behovFromParametere(parametere)

            val minsteinntektOgPeriodeSubsumsjon =
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, ::extractMinsteinntektOgPeriode)

            call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
        }
    }
}

fun validateParameters(parameters: MinsteinntektOgPeriodeParametere) {
    parameters.bruktInntektsPeriode?.let {
        if (it.foersteMaaned.isAfter(it.sisteMaaned)) throw InvalidInnteksperiodeException(
                "Feil bruktInntektsPeriode: foersteMaaned=${it.foersteMaaned} er etter sisteMaaned=${it.sisteMaaned}"
        )
    }
}

fun behovFromParametere(parametere: MinsteinntektOgPeriodeParametere): BehovRequest {
    return BehovRequest(
            parametere.aktorId,
            parametere.vedtakId,
            parametere.beregningsdato,
            harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
            bruktInntektsPeriode = parametere.bruktInntektsPeriode?.let {
                InntektsPeriode(
                        førsteMåned = parametere.bruktInntektsPeriode.foersteMaaned,
                        sisteMåned = parametere.bruktInntektsPeriode.sisteMaaned)
            }
    )
}