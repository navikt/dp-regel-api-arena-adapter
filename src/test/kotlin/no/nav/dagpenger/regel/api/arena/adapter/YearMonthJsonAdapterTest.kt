package no.nav.dagpenger.regel.api.arena.adapter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

internal class YearMonthJsonAdapterTest {
    private val yearMonthJsonAdapter = YearMonthJsonAdapter()

    @Test
    fun toJson() {
        val yearMonth = YearMonth.of(2000, 8)
        assertEquals("2000-08", yearMonthJsonAdapter.toJson(yearMonth))
    }

    @Test
    fun fromJson() {
        val yearMonth = YearMonth.of(2000, 8)
        assertEquals(yearMonth, yearMonthJsonAdapter.fromJson("2000-08"))
    }
}
