package no.nav.dagpenger.regel.api.internal.models

import java.time.LocalDate
import java.time.LocalDateTime

data class GrunnlagSubsumsjon(
    val subsumsjonsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val faktum: GrunnlagFaktum,
    val resultat: GrunnlagResultat,
    val inntekt: Set<InntektGrunnlag>? = null
)

data class GrunnlagResultat(
    val avkortet: Int,
    val uavkortet: Int,
    val beregningsregel: String,
    val harAvkortet: Boolean
)

data class GrunnlagFaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String? = null,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    val manueltGrunnlag: Int?
)