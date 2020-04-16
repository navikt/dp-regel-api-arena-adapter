package no.nav.dagpenger.regel.api.arena.adapter

import java.time.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
