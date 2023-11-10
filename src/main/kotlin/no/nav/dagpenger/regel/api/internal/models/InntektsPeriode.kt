package no.nav.dagpenger.regel.api.internal.models

import java.time.YearMonth

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth,
)
