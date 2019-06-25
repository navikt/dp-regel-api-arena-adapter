package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.Faktum
import no.nav.dagpenger.regel.api.internal.models.Inntekt
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektResultat
import no.nav.dagpenger.regel.api.internal.models.PeriodeResultat
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class ExtractMinsteinntektOgPeriodeTest {

    @Test
    fun `Convert Subsumsjon to MinsteinntektOgPeriodeSubsumsjon`() {
        val result = extractMinsteinntektOgPeriode(
            subsumsjonWithBothResults,
                LocalDateTime.of(2019, 4, 25, 1, 1, 1),
                LocalDateTime.of(2019, 4, 25, 1, 1, 1)
        )

        assertEquals(minsteinntektOgPeriodeSubsumsjon, result)
    }

    @Test
    fun `Converted Subsumsjon with oppfyllerMinsteinntekt false sets periodeAntallUker to null`() {
        val result = extractMinsteinntektOgPeriode(
            subsumsjonWithOppfyllerMinsteinntektFalse,
            LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            LocalDateTime.of(2019, 4, 25, 1, 1, 1)
        )

        assertEquals(false, result.resultat.oppfyllerKravTilMinsteArbeidsinntekt)
        assertNull(result.resultat.periodeAntallUker)
    }

    private val minsteinntektOgPeriodeSubsumsjon = MinsteinntektOgPeriodeSubsumsjon(
        minsteinntektSubsumsjonsId = "sub123456",
        periodeSubsumsjonsId = "sub654321",
        opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
        utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
        parametere = MinsteinntektOgPeriodeRegelfaktum(
            aktorId = "aktoer123",
            vedtakId = 123456,
            beregningsdato = LocalDate.of(2019, 5, 14),
            inntektsId = "inntekt123",
            harAvtjentVerneplikt = true,
            oppfyllerKravTilFangstOgFisk = true,
            bruktInntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                foersteMaaned = YearMonth.of(2018, 5),
                sisteMaaned = YearMonth.of(2019, 1)
            )
        ),
        resultat = MinsteinntektOgPeriodeResultat(
            oppfyllerKravTilMinsteArbeidsinntekt = true,
            periodeAntallUker = 104
        ),
        inntekt = setOf(
            no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                inntekt = 600000,
                andel = 600000,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2018, 5),
                    sisteMaaned = YearMonth.of(2019, 5)
                ),
                inneholderNaeringsinntekter = true,
                periode = 1
            ),
            no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                inntekt = 500000,
                andel = 200000,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2017, 5),
                    sisteMaaned = YearMonth.of(2018, 5)
                ),
                inneholderNaeringsinntekter = false,
                periode = 2
            ),
            no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                inntekt = 400000,
                andel = 300000,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2016, 5),
                    sisteMaaned = YearMonth.of(2017, 5)
                ),
                inneholderNaeringsinntekter = true,
                periode = 3
            )
        ),
        inntektManueltRedigert = true,
        inntektAvvik = true
    )

    private val subsumsjonWithBothResults = Subsumsjon(
        id = "id123",
        behovId = "behov123",
        faktum = Faktum(
            "aktoer123",
            123456,
            beregningsdato = LocalDate.of(2019, 5, 14),
            inntektsId = "inntekt123",
            inntektAvvik = true,
            inntektManueltRedigert = true,
            harAvtjentVerneplikt = true,
            oppfyllerKravTilFangstOgFisk = true,
            bruktInntektsPeriode = InntektsPeriode(
                YearMonth.of(2018, 5),
                YearMonth.of(2019, 1)
            )
        ),
        minsteinntektResultat = MinsteinntektResultat(
            subsumsjonsId = "sub123456",
            sporingsId = "sporing123",
            regelIdentifikator = "minsteinntektregel",
            oppfyllerMinsteinntekt = true,
            minsteinntektInntektsPerioder = listOf(
                Inntekt(
                    inntekt = 600000.toBigDecimal(),
                    andel = 600000.toBigDecimal(),
                    periode = 1,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2018, 5), YearMonth.of(2019, 5)),
                    inneholderFangstOgFisk = true
                ),
                Inntekt(
                    inntekt = 500000.toBigDecimal(),
                    andel = 200000.toBigDecimal(),
                    periode = 2,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2017, 5), YearMonth.of(2018, 5)),
                    inneholderFangstOgFisk = false
                ),
                Inntekt(
                    inntekt = 400000.toBigDecimal(),
                    andel = 300000.toBigDecimal(),
                    periode = 3,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2016, 5), YearMonth.of(2017, 5)),
                    inneholderFangstOgFisk = true
                )
            )
        ),
        periodeResultat = PeriodeResultat(
            subsumsjonsId = "sub654321",
            sporingsId = "sporing321",
            regelIdentifikator = "perioderegel",
            periodeAntallUker = 104
        ),
        grunnlagResultat = null,
        satsResultat = null,
        problem = null
    )

    private val subsumsjonWithOppfyllerMinsteinntektFalse = Subsumsjon(
        id = "id123",
        behovId = "behov123",
        faktum = Faktum(
            "aktoer123",
            123456,
            beregningsdato = LocalDate.of(2019, 5, 14),
            inntektsId = "inntekt123"
        ),
        minsteinntektResultat = MinsteinntektResultat(
            subsumsjonsId = "subAvslag",
            sporingsId = "sporing123",
            regelIdentifikator = "minsteinntektregel",
            oppfyllerMinsteinntekt = false,
            minsteinntektInntektsPerioder = listOf(
                Inntekt(
                    inntekt = 0.toBigDecimal(),
                    periode = 1,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2018, 5), YearMonth.of(2019, 5)),
                    inneholderFangstOgFisk = true
                ),
                Inntekt(
                    inntekt = 0.toBigDecimal(),
                    periode = 2,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2017, 5), YearMonth.of(2018, 5)),
                    inneholderFangstOgFisk = false
                ),
                Inntekt(
                    inntekt = 0.toBigDecimal(),
                    periode = 3,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2016, 5), YearMonth.of(2017, 5)),
                    inneholderFangstOgFisk = true
                )
            )
        ),
        periodeResultat = PeriodeResultat(
            subsumsjonsId = "sub654321",
            sporingsId = "sporing321",
            regelIdentifikator = "perioderegel",
            periodeAntallUker = 104
        ),
        grunnlagResultat = null,
        satsResultat = null,
        problem = null
    )
}