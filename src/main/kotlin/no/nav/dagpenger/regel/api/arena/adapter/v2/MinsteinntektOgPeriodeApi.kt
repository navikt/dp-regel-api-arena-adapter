package no.nav.dagpenger.regel.api.arena.adapter.v2

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internalV2.BehovRequest
import no.nav.dagpenger.regel.api.internalV2.RegelApiBehovHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiStatusHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiSubsumsjonHttpClient
import no.nav.dagpenger.regel.api.internalV2.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internalV2.models.Subsumsjon
import java.time.LocalDateTime

fun Route.MinsteinntektOgPeriodeApiV2(
    behovHttpClient: RegelApiBehovHttpClient,
    statusHttpClient: RegelApiStatusHttpClient,
    subsumsjonHttpClient: RegelApiSubsumsjonHttpClient
) {

    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektOgPeriodeParametere>()

            val behovRequest = BehovRequest(
                parametere.aktorId,
                parametere.vedtakId,
                parametere.beregningsdato,
                harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
                oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
                bruktInntektsPeriode = parametere.bruktInntektsPeriode?.let { InntektsPeriode(
                    førsteMåned = parametere.bruktInntektsPeriode.foersteMaaned,
                    sisteMåned = parametere.bruktInntektsPeriode.sisteMaaned)
                }
            )

            val opprettet = LocalDateTime.now()

            val statusUrl = behovHttpClient.run(behovRequest)
            val subsumsjonLocation = statusHttpClient.pollStatus(statusUrl)
            val subsumsjon = subsumsjonHttpClient.getSubsumsjon(subsumsjonLocation)

            val utfort = LocalDateTime.now()

            val minsteinntektOgPeriodeSubsumsjon = extractMinsteinntektOgPeriode(subsumsjon, opprettet, utfort)

            call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
        }
    }
}

private fun extractMinsteinntektOgPeriode(
    subsumsjon: Subsumsjon,
    opprettet: LocalDateTime,
    utfort: LocalDateTime
): MinsteinntektOgPeriodeSubsumsjon {

    val faktum = subsumsjon.faktum
    val minsteinntektResultat = subsumsjon.minsteinntektResultat ?: throw MissingSubsumsjonDataException("Missing minsteinntektResultat")
    val periodeResultat = subsumsjon.periodeResultat ?: throw MissingSubsumsjonDataException("Missing periodeResultat")

    return MinsteinntektOgPeriodeSubsumsjon(
        minsteinntektSubsumsjonsId = minsteinntektResultat.subsumsjonsId,
        periodeSubsumsjonsId = periodeResultat.subsumsjonsId,
        opprettet = opprettet,
        utfort = utfort,
        parametere = MinsteinntektOgPeriodeRegelfaktum(
            aktorId = faktum.aktorId,
            vedtakId = faktum.vedtakId,
            beregningsdato = faktum.beregningsdato,
            inntektsId = faktum.inntektsId ?: throw MissingSubsumsjonDataException("Missing faktum inntektId"),
            harAvtjentVerneplikt = faktum.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = faktum.oppfyllerKravTilFangstOgFisk,
            bruktInntektsPeriode = faktum.bruktInntektsPeriode?.let { no.nav.dagpenger.regel.api.arena.adapter.v2.models.InntektsPeriode(
                foersteMaaned = faktum.bruktInntektsPeriode.førsteMåned,
                sisteMaaned = faktum.bruktInntektsPeriode.sisteMåned)
            }
        ),
        resultat = MinsteinntektOgPeriodeResultat(
            oppfyllerKravTilMinsteArbeidsinntekt = minsteinntektResultat.oppfyllerMinsteinntekt,
            periodeAntallUker = periodeResultat.periodeAntallUker
        ),
        inntekt = minsteinntektResultat.minsteinntektInntektsPerioder.map {
            Inntekt(
                inntekt = it.inntekt,
                periode = it.periode,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v2.models.InntektsPeriode(
                    foersteMaaned = it.inntektsPeriode.førsteMåned,
                    sisteMaaned = it.inntektsPeriode.sisteMåned
                ),
                andel = it.andel,
                inneholderNaeringsinntekter = it.inneholderFangstOgFisk
            )
        }.toSet(),
        inntektManueltRedigert = faktum.inntektManueltRedigert,
        inntektAvvik = faktum.inntektAvvik
    )
}