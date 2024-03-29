package no.nav.dagpenger.regel.api.arena.adapter.v1.models

import java.time.YearMonth

data class InntektsPeriode(
    val foersteMaaned: YearMonth,
    val sisteMaaned: YearMonth,
)
