package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode

import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.MinsteinntektFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.PeriodeFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.PeriodeSubsumsjon
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime

data class MinsteinntektOgPeriodeSubsumsjon(
    val minsteinntektSubsumsjonsId: String,
    val periodeSubsumsjonsId: String,
    val opprettet: LocalDateTime, // todo: ZonedDateTime?
    val utfort: LocalDateTime, // todo: ZonedDateTime?,
    val parametere: MinsteinntektOgPeriodeRegelfaktum,
    val resultat: MinsteinntektOgPeriodeResultat,
    val inntekt: Set<Inntekt>
)

data class MinsteinntektOgPeriodeRegelfaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilFangstOgFisk: Boolean? = false,
    val bruktInntektsPeriode: InntektsPeriode? = null
)

data class MinsteinntektOgPeriodeResultat(
    val oppfyllerKravTilMinsteArbeidsinntekt: Boolean,
    val periodeAntallUker: Int? = null
) {
    /*
    init {
        val gyldigeUker = setOf(26, 52, 104)
        if (!gyldigeUker.contains(periodeAntallUker)) {
            throw IllegalArgumentException("Ugyldig antall uker for minsteinntekt, gyldige verdier er ${gyldigeUker.joinToString { "$it" }}")
        }
    }
    */
}

fun mergeMinsteinntektOgPeriodeSubsumsjon(
    minsteinntektSubsumsjon: MinsteinntektSubsumsjon,
    periodeSubsumsjon: PeriodeSubsumsjon
): MinsteinntektOgPeriodeSubsumsjon {

    val minsteinntektFaktum = minsteinntektSubsumsjon.faktum
    val periodeFaktum = periodeSubsumsjon.faktum

    if (!compareFields(minsteinntektFaktum, periodeFaktum)) throw UnMatchingFaktumException("Minsteinntekt and periode faktum dont match")

    return MinsteinntektOgPeriodeSubsumsjon(
        minsteinntektSubsumsjon.subsumsjonsId,
        periodeSubsumsjon.subsumsjonsId,
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

fun compareFields(minsteinntektFaktum: MinsteinntektFaktum, periodeFaktum: PeriodeFaktum): Boolean {

    if (minsteinntektFaktum.aktorId.equals(periodeFaktum.aktorId) &&
        minsteinntektFaktum.beregningsdato.equals(periodeFaktum.beregningsdato) &&
        minsteinntektFaktum.vedtakId.equals(periodeFaktum.vedtakId) &&
        minsteinntektFaktum.harAvtjentVerneplikt == periodeFaktum.harAvtjentVerneplikt &&
        minsteinntektFaktum.oppfyllerKravTilFangstOgFisk == periodeFaktum.oppfyllerKravTilFangstOgFisk &&
        minsteinntektFaktum.bruktInntektsPeriode?.equals(periodeFaktum.bruktInntektsPeriode) ?: (periodeFaktum.bruktInntektsPeriode === null)) {
        return true
    }
    return false
}

class UnMatchingFaktumException(override val message: String) : RuntimeException(message)