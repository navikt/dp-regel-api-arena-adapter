package no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag

import java.time.LocalDate

data class DagpengegrunnlagResultatParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    val grunnlag: Int? = null
)
