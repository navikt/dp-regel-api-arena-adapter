package no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.periode.PeriodeSubsumsjon
import java.time.LocalDateTime

data class MinsteinntektOgPeriodeSubsumsjon(
    val subsumsjonsId: String,
    val opprettet: LocalDateTime, // todo: ZonedDateTime?
    val utfort: LocalDateTime, // todo: ZonedDateTime?,
    val parametere: MinsteinntektOgPeriodeRegelfaktum,
    val resultat: MinsteinntektOgPeriodeResultat,
    val inntekt: Set<Inntekt>
)

fun mergeMinsteinntektOgPeriodeSubsumsjon(
    minsteinntektSubsumsjon: MinsteinntektSubsumsjon,
    periodeSubsumsjon: PeriodeSubsumsjon
): MinsteinntektOgPeriodeSubsumsjon {

    val minsteinntektFaktum = minsteinntektSubsumsjon.faktum
    val periodeFaktum = periodeSubsumsjon.faktum

    return MinsteinntektOgPeriodeSubsumsjon(
        minsteinntektSubsumsjon.subsumsjonsId,
        minsteinntektSubsumsjon.opprettet,
        minsteinntektSubsumsjon.utfort,
        MinsteinntektOgPeriodeRegelfaktum(
            minsteinntektFaktum.aktorId,
            minsteinntektFaktum.vedtakId,
            minsteinntektFaktum.beregningsdato,
            minsteinntektFaktum.inntektsId,
            minsteinntektFaktum.harAvtjentVerneplikt,
            minsteinntektFaktum.oppfyllerKravTilFangstOgFisk,
            minsteinntektFaktum.bruktInntektsPeriode
        ),
        MinsteinntektOgPeriodeResultat(
            minsteinntektSubsumsjon.resultat.oppfyllerKravTilMinsteArbeidsinntekt,
            periodeSubsumsjon.resultat.antallUker
        ),
        minsteinntektSubsumsjon.inntekt
    )
}