package no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag

import java.time.YearMonth

data class InntektsPeriode(
    val foersteMaaned: YearMonth,
    val sisteMaaned: YearMonth
)
