package no.nav.dagpenger.regel.api.internal.models

data class InntjeningsperiodeParametre(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val inntektsId: String
)