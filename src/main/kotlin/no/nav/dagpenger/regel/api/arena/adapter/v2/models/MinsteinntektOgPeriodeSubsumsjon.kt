package no.nav.dagpenger.regel.api.arena.adapter.v2.models

import java.time.LocalDate
import java.time.LocalDateTime

data class MinsteinntektOgPeriodeSubsumsjon(
    val minsteinntektSubsumsjonsId: String,
    val periodeSubsumsjonsId: String? = null,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: MinsteinntektOgPeriodeRegelfaktum,
    val resultat: MinsteinntektOgPeriodeResultat,
    val inntekt: Set<Inntekt>,
    val inntektManueltRedigert: Boolean? = null,
    val inntektAvvik: Boolean? = null
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
)