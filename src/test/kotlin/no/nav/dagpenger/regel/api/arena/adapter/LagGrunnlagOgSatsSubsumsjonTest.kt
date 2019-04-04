package no.nav.dagpenger.regel.api.arena.adapter

import no.nav.dagpenger.regel.api.arena.adapter.v1.mergeGrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.internal.models.GrunnlagFaktum
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internal.models.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.InntektGrunnlag
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.SatsFaktum
import no.nav.dagpenger.regel.api.internal.models.SatsResultat
import no.nav.dagpenger.regel.api.internal.models.SatsSubsumsjon
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
                0,
                0
            ),
            GrunnlagResultat(
                555,
                666,
                "ArbeidsinntektSiste12"
            ),
            setOf(
                InntektGrunnlag(
                    inntekt = 0,
                    periode = 1,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2018, 2), YearMonth.of(2019, 1)),
                    inneholderFangstOgFisk = false
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
                10000
            ),
            SatsResultat(
                240,
                240,
                false
            )
        )

        val grunnlagOgSatsSubsumsjon =
            mergeGrunnlagOgSatsSubsumsjon(
                grunnlagSubsumsjon,
                satsSubsumsjon
            )

        assertEquals("222", grunnlagOgSatsSubsumsjon.parametere.aktorId)
        assertEquals(555, grunnlagOgSatsSubsumsjon.resultat.grunnlag!!.avkortet)
        assertEquals(666, grunnlagOgSatsSubsumsjon.resultat.grunnlag!!.uavkortet)
        assertEquals(GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019, grunnlagOgSatsSubsumsjon.resultat.beregningsRegel)

        assertEquals(240, grunnlagOgSatsSubsumsjon.resultat.sats.dagsats)
        assertEquals(240, grunnlagOgSatsSubsumsjon.resultat.sats.ukesats)
    }
}