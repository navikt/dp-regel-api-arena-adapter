package no.nav.dagpenger.regel.api.internal.models

import java.time.LocalDate

data class SatsParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val antallBarn: Int? = 0
)