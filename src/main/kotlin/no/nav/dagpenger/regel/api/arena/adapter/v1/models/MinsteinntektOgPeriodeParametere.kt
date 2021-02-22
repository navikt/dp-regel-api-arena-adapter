package no.nav.dagpenger.regel.api.arena.adapter.v1.models

import java.time.LocalDate

data class MinsteinntektOgPeriodeParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilLaerling: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean? = false,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val virkningstidspunkt: LocalDate = beregningsdato
)
