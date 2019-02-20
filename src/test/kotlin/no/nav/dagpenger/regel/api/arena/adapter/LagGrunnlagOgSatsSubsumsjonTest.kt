package no.nav.dagpenger.regel.api.arena.adapter

import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.GrunnlagFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.GrunnlagResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.mapGrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.mergeGrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SatsFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.InntektsPeriode
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class LagGrunnlagOgSatsSubsumsjonTest {

    @Test
    fun ` skal kunne merge grunnlagOgSatsSubsumsjon `() {
        val grunnlagSubsumsjon = GrunnlagSubsumsjon(
            "123",
            LocalDateTime.now(),
            LocalDateTime.now(),
            GrunnlagFaktum(
                "222",
                999,
                LocalDate.of(20, 2, 19),
                "888",
                false,
                false,
                0),
            GrunnlagResultat(
                555,
                666),
            setOf(
                Inntekt(
                    inntekt = 0,
                    periode = 1,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2018, 2), YearMonth.of(2019, 1)),
                    inneholderNaeringsinntekter = false,
                    andel = 0
                )
            )
        )

        val satsSubsumsjon = SatsSubsumsjon(
            "123",
            LocalDateTime.now(),
            LocalDateTime.now(),
            SatsFaktum(
                "222",
                999,
                LocalDate.of(20, 2, 19),
                10000,
                0
            ),
            SatsResultat(
                240,
                240
            )
        )

        val grunnlagOgSatsSubsumsjon = mergeGrunnlagOgSatsSubsumsjon(grunnlagSubsumsjon, satsSubsumsjon)

        assertEquals("222", grunnlagOgSatsSubsumsjon.parametere.aktorId)
        assertEquals(555, grunnlagOgSatsSubsumsjon.resultat.grunnlag.avkortet)
        assertEquals(666, grunnlagOgSatsSubsumsjon.resultat.grunnlag.uavkortet)

        assertEquals(240, grunnlagOgSatsSubsumsjon.resultat.sats.dagsats)
        assertEquals(240, grunnlagOgSatsSubsumsjon.resultat.sats.ukesats)
    }

    @Test
    fun ` skal kunne mappe grunnlagOgSatsSubsumsjon `() {

        val satsSubsumsjon = SatsSubsumsjon(
            "123",
            LocalDateTime.now(),
            LocalDateTime.now(),
            SatsFaktum(
                "222",
                999,
                LocalDate.now(),
                10000,
                0
            ),
            SatsResultat(
                240,
                240
            )

        )

        val grunnlagSatsSubsumsjon = mapGrunnlagOgSatsSubsumsjon(satsSubsumsjon)

        assertEquals("222", grunnlagSatsSubsumsjon.parametere.aktorId)
        assertEquals(240, grunnlagSatsSubsumsjon.resultat.sats.dagsats)
        assertEquals(240, grunnlagSatsSubsumsjon.resultat.sats.ukesats)
    }
}