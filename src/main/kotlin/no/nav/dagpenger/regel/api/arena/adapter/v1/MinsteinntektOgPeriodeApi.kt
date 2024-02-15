package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.RegelKontekst
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internal.extractMinsteinntektOgPeriode
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode

private val sikkerlogg = KotlinLogging.logger("tjenestekall.minsteinntektOgPeriodeApi")

internal fun Route.minsteinntektOgPeriodeApi(synchronousSubsumsjonClient: SynchronousSubsumsjonClient) {
    route("/minsteinntekt") {
        post {
            withContext(Dispatchers.IO) {
                val parametere = call.receive<MinsteinntektOgPeriodeParametere>()

                parametere.validate()

                val behovRequest = behovFromParametere(parametere)

                val minsteinntektOgPeriodeSubsumsjon: MinsteinntektOgPeriodeSubsumsjon =
                    synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                        behovRequest,
                        ::extractMinsteinntektOgPeriode,
                    )

                call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
            }
        }
    }
}

fun MinsteinntektOgPeriodeParametere.validate() {
    this.bruktInntektsPeriode?.let {
        if (it.foersteMaaned.isAfter(it.sisteMaaned)) {
            throw InvalidInnteksperiodeException(
                "Feil bruktInntektsPeriode: foersteMaaned=${it.foersteMaaned} er etter sisteMaaned=${it.sisteMaaned}",
            )
        }
    }
    if (this.oppfyllerKravTilLaerling && this.harAvtjentVerneplikt == true) {
        throw UgyldigParameterkombinasjonException(
            "harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig",
        )
    }
}

fun behovFromParametere(parametere: MinsteinntektOgPeriodeParametere): BehovRequest {
    return BehovRequest(
        aktorId = parametere.aktorId,
        vedtakId = parametere.vedtakId,
        regelkontekst = RegelKontekst(id = parametere.vedtakId.toString()),
        beregningsdato = parametere.beregningsdato,
        harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
        oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
        lærling = parametere.oppfyllerKravTilLaerling,
        bruktInntektsPeriode =
        parametere.bruktInntektsPeriode?.let {
            InntektsPeriode(
                førsteMåned = it.foersteMaaned,
                sisteMåned = it.sisteMaaned,
            )
        },
        regelverksdato = parametere.regelverksdato,
    ).also {
        withLoggingContext("requestId" to it.requestId) {
            sikkerlogg.info { "Lager behov for $parametere" }
        }
    }
}
