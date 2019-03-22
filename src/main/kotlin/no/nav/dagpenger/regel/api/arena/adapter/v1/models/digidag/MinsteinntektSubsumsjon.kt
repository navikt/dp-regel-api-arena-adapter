package no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag

import java.time.LocalDate
import java.time.LocalDateTime

data class MinsteinntektSubsumsjon(
    val subsumsjonsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val faktum: MinsteinntektFaktum,
    val resultat: MinsteinntektResultat,
    val inntekt: List<Inntekt>
)

data class MinsteinntektResultat(
    val oppfyllerKravTilMinsteArbeidsinntekt: Boolean
)

data class MinsteinntektFaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilFangstOgFisk: Boolean? = false,
    val bruktInntektsPeriode: InntektsPeriode? = null
)