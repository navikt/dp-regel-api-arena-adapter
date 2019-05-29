package no.nav.dagpenger.regel.api.internalV2

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v2.MissingSubsumsjonDataException
import no.nav.dagpenger.regel.api.internalV2.models.Subsumsjon
import java.time.LocalDateTime

fun extractMinsteinntektOgPeriode(
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
            bruktInntektsPeriode = faktum.bruktInntektsPeriode?.let { InntektsPeriode(
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
                inntektsPeriode = InntektsPeriode(
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