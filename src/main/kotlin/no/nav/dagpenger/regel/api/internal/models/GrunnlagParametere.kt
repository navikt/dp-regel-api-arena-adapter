package no.nav.dagpenger.regel.api.internal.models

import java.time.LocalDate

data class GrunnlagParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val bruktInntektsPeriode: InntektsPeriode? = null
)