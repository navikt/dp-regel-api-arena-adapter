package no.nav.dagpenger.regel.api.arena.adapter.v2.models

data class InntjeningsperiodeParametre(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val inntektsId: String
)