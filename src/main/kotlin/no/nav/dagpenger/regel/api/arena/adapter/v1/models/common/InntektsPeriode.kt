package no.nav.dagpenger.regel.api.arena.adapter.v1.models.common

import com.squareup.moshi.Json
import java.time.YearMonth

data class InntektsPeriode(
    @Json(name = "førsteMåned") val foersteMaaned: YearMonth? = null,
    @Json(name = "sisteMåned") val sisteMaaned: YearMonth
)
