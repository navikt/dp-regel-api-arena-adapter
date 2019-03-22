package no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena

import java.time.LocalDate

data class GrunnlagOgSatsParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    var grunnlag: Int? = null
)
