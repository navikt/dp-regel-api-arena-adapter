package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.internal.models.Faktum
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internal.models.Inntekt
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.SatsResultat
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class ExtractGrunnlagOgSatsTest {

    @Test
    fun `Convert Subsumsjon to GrunnlagOgSatsSubsumsjon`() {
        val result = extractGrunnlagOgSats(
            subsumsjon,
            LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            LocalDateTime.of(2019, 4, 25, 1, 1, 1))

        assertEquals(grunnlagOgSatsSubsumsjon, result)
    }

    private val grunnlagOgSatsSubsumsjon = GrunnlagOgSatsSubsumsjon(
        grunnlagSubsumsjonsId = "sub123456",
        satsSubsumsjonsId = "sub654321",
        opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
        utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
        parametere = GrunnlagOgSatsRegelFaktum(
            aktorId = "aktoer123",
            vedtakId = 123456,
            beregningsdato = LocalDate.of(2019, 5, 14),
            inntektsId = "inntekt123",
            harAvtjentVerneplikt = true,
            oppfyllerKravTilFangstOgFisk = true,
            antallBarn = 5,
            grunnlag = 1000
        ),
        resultat = GrunnlagOgSatsResultat(
            grunnlag = Grunnlag(
                avkortet = 12345,
                uavkortet = 12345
            ),
            sats = Sats(400, 2000),
            beregningsRegel = GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019,
            benyttet90ProsentRegel = true
        ),
        inntekt = setOf(
            no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                inntekt = 600000,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2018, 5),
                    sisteMaaned = YearMonth.of(2019, 5)
                ),
                inneholderNaeringsinntekter = true,
                periode = 1
            ),
            no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                inntekt = 500000,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2017, 5),
                    sisteMaaned = YearMonth.of(2018, 5)
                ),
                inneholderNaeringsinntekter = false,
                periode = 2
            ),
            no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                inntekt = 400000,
                inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2016, 5),
                    sisteMaaned = YearMonth.of(2017, 5)
                ),
                inneholderNaeringsinntekter = true,
                periode = 3)
        ),
        inntektManueltRedigert = true,
        inntektAvvik = true
    )

    private val subsumsjon = Subsumsjon(
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
            antallBarn = 5,
            manueltGrunnlag = 1000,
            bruktInntektsPeriode = InntektsPeriode(
                YearMonth.of(2018, 5),
                YearMonth.of(2019, 1)
            )
        ),
        grunnlagResultat = GrunnlagResultat(
            subsumsjonsId = "sub123456",
            sporingsId = "sporing123",
            regelIdentifikator = "grunnlagregel",
            avkortet = BigDecimal(12345),
            uavkortet = BigDecimal(12345),
            beregningsregel = "ArbeidsinntektSiste12",
            harAvkortet = true,
            grunnlagInntektsPerioder = listOf(
                Inntekt(
                    inntekt = 600000.toBigDecimal(),
                    periode = 1,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2018, 5), YearMonth.of(2019, 5)),
                    inneholderFangstOgFisk = true),
                Inntekt(
                    inntekt = 500000.toBigDecimal(),
                    periode = 2,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2017, 5), YearMonth.of(2018, 5)),
                    inneholderFangstOgFisk = false),
                Inntekt(
                    inntekt = 400000.toBigDecimal(),
                    periode = 3,
                    inntektsPeriode = InntektsPeriode(YearMonth.of(2016, 5), YearMonth.of(2017, 5)),
                    inneholderFangstOgFisk = true)

            )
        ),
        satsResultat = SatsResultat(
            subsumsjonsId = "sub654321",
            sporingsId = "sporing321",
            regelIdentifikator = "satsregel",
            dagsats = 400,
            ukesats = 2000,
            benyttet90ProsentRegel = true
        ),
        minsteinntektResultat = null,
        periodeResultat = null,
        problem = null
    )
}