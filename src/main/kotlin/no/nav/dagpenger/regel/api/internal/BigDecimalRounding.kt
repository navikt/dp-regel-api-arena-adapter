package no.nav.dagpenger.regel.api.internal

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.round(): BigDecimal = this.setScale(0, RoundingMode.HALF_UP)
