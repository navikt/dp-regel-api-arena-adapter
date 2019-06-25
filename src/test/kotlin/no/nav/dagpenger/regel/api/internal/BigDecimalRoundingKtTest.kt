package no.nav.dagpenger.regel.api.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

internal class BigDecimalRoundingKtTest {

    @Test
    fun ` BigDecimal rounding logic`() {
        90.toBigDecimal().round() shouldBe 90.toBigDecimal()
        90.49.toBigDecimal().round() shouldBe 90.toBigDecimal()
        90.51.toBigDecimal().round() shouldBe 91.toBigDecimal()
    }
}