package no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena

import java.time.LocalDate
import java.time.LocalDateTime

data class MinsteinntektOgPeriodeSubsumsjon(
    val minsteinntektSubsumsjonsId: String,
    val periodeSubsumsjonsId: String? = null,
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
