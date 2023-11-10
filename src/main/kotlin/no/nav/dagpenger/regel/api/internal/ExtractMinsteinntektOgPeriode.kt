package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.v1.MissingSubsumsjonDataException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektRegel
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import java.time.LocalDateTime

fun extractMinsteinntektOgPeriode(
    subsumsjon: Subsumsjon,
    opprettet: LocalDateTime,
    utfort: LocalDateTime,
): MinsteinntektOgPeriodeSubsumsjon {
    val faktum = subsumsjon.faktum
    val minsteinntektResultat =
        subsumsjon.minsteinntektResultat ?: throw MissingSubsumsjonDataException("Missing minsteinntektResultat")

    // TODO: Fix proper handling of inngangsvilkår
    val periodeResultat = if (minsteinntektResultat.oppfyllerMinsteinntekt) {
        subsumsjon.periodeResultat ?: throw MissingSubsumsjonDataException("Missing periodeResultat")
    } else {
        null
    }

    return MinsteinntektOgPeriodeSubsumsjon(
        minsteinntektSubsumsjonsId = minsteinntektResultat.subsumsjonsId,
        periodeSubsumsjonsId = periodeResultat?.subsumsjonsId,
        opprettet = opprettet,
        utfort = utfort,
        parametere = MinsteinntektOgPeriodeRegelfaktum(
            aktorId = faktum.aktorId,
            vedtakId = faktum.regelkontekst.id.toInt(),
            beregningsdato = faktum.beregningsdato,
            inntektsId = faktum.inntektsId ?: throw MissingSubsumsjonDataException("Missing faktum inntektId"),
            regelverksdato = faktum.regelverksdato,
            harAvtjentVerneplikt = faktum.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = faktum.oppfyllerKravTilFangstOgFisk,
            oppfyllerKravTilLaerling = faktum.lærling,
            bruktInntektsPeriode = faktum.bruktInntektsPeriode?.let {
                InntektsPeriode(
                    foersteMaaned = faktum.bruktInntektsPeriode.førsteMåned,
                    sisteMaaned = faktum.bruktInntektsPeriode.sisteMåned,
                )
            },
        ),
        resultat = MinsteinntektOgPeriodeResultat(
            oppfyllerKravTilMinsteArbeidsinntekt = minsteinntektResultat.oppfyllerMinsteinntekt,
            periodeAntallUker = periodeResultat?.periodeAntallUker,
            minsteinntektRegel = MinsteinntektRegel.valueOf(minsteinntektResultat.beregningsregel.name),
        ),
        inntekt = minsteinntektResultat.minsteinntektInntektsPerioder.map {
            Inntekt(
                inntekt = it.inntekt.round().toInt(),
                periode = it.periode,
                inntektsPeriode = InntektsPeriode(
                    foersteMaaned = it.inntektsPeriode.førsteMåned,
                    sisteMaaned = it.inntektsPeriode.sisteMåned,
                ),
                andel = it.andel?.round()?.toInt(),
                inneholderNaeringsinntekter = it.inneholderFangstOgFisk,
            )
        }.toSet(),
        inntektManueltRedigert = faktum.inntektManueltRedigert,
        inntektAvvik = faktum.inntektAvvik,
    )
}
