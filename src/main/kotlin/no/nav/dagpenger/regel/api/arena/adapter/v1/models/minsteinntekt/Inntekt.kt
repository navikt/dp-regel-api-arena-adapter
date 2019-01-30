package no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt

data class Inntekt(
    val inntekt: Int,
    val periode: Int, // todo: enum?
    val inneholderNaeringsinntekter: Boolean,
    val andel: Int
)