package no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt

import java.time.LocalDate

data class MinsteinntektInnParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilFangstOgFisk: Boolean? = false,
    val bruktInntektsPeriode: InntektsPeriode? = null
)