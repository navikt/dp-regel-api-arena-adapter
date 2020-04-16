package no.nav.dagpenger.regel.api.arena.adapter

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LocalDateTimeJsonAdapterTest {

    private val localDateTimeJsonAdapter = LocalDateTimeJsonAdapter()

    @Test
    fun toJson() {
        val localDateTime = LocalDateTime.of(2000, 8, 11, 15, 30, 11)
        assertEquals("2000-08-11T15:30:11", localDateTimeJsonAdapter.toJson(localDateTime))
    }

    @Test
    fun fromJson() {
        val localDateTime = LocalDateTime.of(2000, 8, 11, 15, 30, 11)
        assertEquals(localDateTime, localDateTimeJsonAdapter.fromJson("2000-08-11T15:30:11"))
    }
}
