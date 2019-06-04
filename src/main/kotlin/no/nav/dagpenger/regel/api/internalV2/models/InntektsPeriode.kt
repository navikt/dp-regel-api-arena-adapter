package no.nav.dagpenger.regel.api.internalV2.models

import java.time.YearMonth

data class InntektsPeriode(
    val førsteMåned: YearMonth,
    val sisteMåned: YearMonth
)