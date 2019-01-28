package no.nav.dagpenger.regel.api.arena.adapter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class LocalDateJsonAdapterTest {

    private val localDateJsonAdapter = LocalDateJsonAdapter()

    @Test
    fun toJson() {
        val localDate = LocalDate.of(2000, 8, 11)
        assertEquals("2000-08-11", localDateJsonAdapter.toJson(localDate))
    }

    @Test
    fun fromJson() {
        val localDate = LocalDate.of(2000, 8, 11)
        assertEquals(localDate, localDateJsonAdapter.fromJson("2000-08-11"))
    }
}