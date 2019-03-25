package no.nav.dagpenger.regel.api.arena.adapter

import no.nav.dagpenger.regel.api.arena.adapter.v1.compareFields
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektFaktum
import no.nav.dagpenger.regel.api.internal.models.PeriodeFaktum
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class MinsteinntektPeriodeCompareFieldsTest {

    @Test
    fun ` should return true if fields are the same `() {
        val minsteinntektFaktum = MinsteinntektFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            "789"
        )

        val periodeFaktum = PeriodeFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19)
        )

        assertTrue(
            compareFields(
                minsteinntektFaktum,
                periodeFaktum
            )
        )
    }

    @Test
    fun ` should return false if fields are not the same `() {
        val minsteinntektFaktum = MinsteinntektFaktum(
            "222",
            456,
            LocalDate.of(2019, 2, 19),
            "789"
        )

        val periodeFaktum = PeriodeFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19)
        )

        assertFalse(
            compareFields(
                minsteinntektFaktum,
                periodeFaktum
            )
        )
    }

    @Test
    fun ` should return false if only minsteinntekt has verneplikt `() {
        val minsteinntektFaktum = MinsteinntektFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            "789",
            true
        )

        val periodeFaktum = PeriodeFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19)
        )

        assertFalse(
            compareFields(
                minsteinntektFaktum,
                periodeFaktum
            )
        )
    }

    @Test
    fun ` should return false if only one has bruktInntektsperiode `() {
        val minsteinntektFaktum = MinsteinntektFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            "789",
            bruktInntektsPeriode = InntektsPeriode(YearMonth.of(18, 9), YearMonth.of(18, 10))
        )

        val periodeFaktum = PeriodeFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19)
        )

        assertFalse(
            compareFields(
                minsteinntektFaktum,
                periodeFaktum
            )
        )
    }

    @Test
    fun ` should return false if bruktInntektsperiode is not the same`() {
        val minsteinntektFaktum = MinsteinntektFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            "789",
            bruktInntektsPeriode = InntektsPeriode(YearMonth.of(18, 9), YearMonth.of(18, 10))
        )

        val periodeFaktum = PeriodeFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            bruktInntektsPeriode = InntektsPeriode(YearMonth.of(18, 8), YearMonth.of(18, 10))
        )

        assertFalse(
            compareFields(
                minsteinntektFaktum,
                periodeFaktum
            )
        )
    }

    @Test
    fun ` should return true if bruktInntektsperiode is the same`() {
        val minsteinntektFaktum = MinsteinntektFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            "789",
            bruktInntektsPeriode = InntektsPeriode(YearMonth.of(18, 9), YearMonth.of(18, 10))
        )

        val periodeFaktum = PeriodeFaktum(
            "123",
            456,
            LocalDate.of(2019, 2, 19),
            bruktInntektsPeriode = InntektsPeriode(YearMonth.of(18, 9), YearMonth.of(18, 10))
        )

        assertTrue(
            compareFields(
                minsteinntektFaktum,
                periodeFaktum
            )
        )
    }
}