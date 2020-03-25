package no.nav.dagpenger.regel.api.arena.adapter.v2.models

import java.time.LocalDate

data class GrunnlagOgSatsParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    var grunnlag: Int? = null
)
