package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.arena.adapter.regelApiAdapter
import no.nav.dagpenger.regel.api.internal.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.internal.models.GrunnlagFaktum
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internal.models.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.InntektGrunnlag
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.SatsFaktum
import no.nav.dagpenger.regel.api.internal.models.SatsResultat
import no.nav.dagpenger.regel.api.internal.models.SatsSubsumsjon
import no.nav.dagpenger.regel.api.internal.sats.SynchronousSats
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class GrunnlagOgSatsSpec {

    val beregningsdato = LocalDate.of(2019, 2, 10)
    val localDateTime = LocalDateTime.of(2000, 8, 11, 15, 30, 11)

    @Test
    fun `Grunnlag and Sats API specification test - Should match json field names and formats`() {

        val synchronousGrunnlag: SynchronousGrunnlag = mockk()
        val synchronousSats: SynchronousSats = mockk()

        every { runBlocking { synchronousGrunnlag.getGrunnlagSynchronously(parametere = any()) } } returns grunnlagSubsumsjon()
        every { runBlocking { synchronousSats.getSatsSynchronously(parametere = any()) } } returns satsSubsumsjon()

        withTestApplication({
            regelApiAdapter(
                mockk(),
                mockk(),
                synchronousGrunnlag,
                synchronousSats
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/dagpengegrunnlag") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }

                """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expectedJson, response.content)
            }
        }
    }

    private val expectedJson =
        """{"grunnlagSubsumsjonsId":"1234","satsSubsumsjonsId":"4567","opprettet":"2000-08-11T15:30:11","utfort":"2000-08-11T15:30:11","parametere":{"aktorId":"1234","vedtakId":123,"beregningsdato":"2019-02-10","inntektsId":"1234","harAvtjentVerneplikt":false,"oppfyllerKravTilFangstOgFisk":false,"antallBarn":0,"grunnlag":12345},"resultat":{"grunnlag":{"avkortet":12345,"uavkortet":12345},"sats":{"dagsats":124,"ukesats":234},"beregningsRegel":"ORDINAER_OVER_6G_SISTE_2019","benyttet90ProsentRegel":false},"inntekt":[{"inntekt":4999423,"periode":1,"inntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"},"inneholderNaeringsinntekter":false}]}"""

    private fun satsSubsumsjon(): SatsSubsumsjon {
        return SatsSubsumsjon(
            subsumsjonsId = "4567",
            opprettet = localDateTime,
            utfort = localDateTime,
            faktum = SatsFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = beregningsdato,
                antallBarn = 0
            ),
            resultat = SatsResultat(
                dagsats = 124,
                ukesats = 234,
                benyttet90ProsentRegel = false
            )

        )
    }

    private fun grunnlagSubsumsjon(): GrunnlagSubsumsjon {
        return GrunnlagSubsumsjon(
            subsumsjonsId = "1234",
            opprettet = localDateTime,
            utfort = localDateTime,
            faktum = GrunnlagFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = beregningsdato,
                inntektsId = "1234",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                manueltGrunnlag = 12345

            ),
            resultat = GrunnlagResultat(
                avkortet = 12345,
                uavkortet = 12345,
                beregningsregel = "ArbeidsinntektSiste12"
            ),
            inntekt = setOf(
                InntektGrunnlag(
                    inntekt = 4999423,
                    inntektsPeriode = InntektsPeriode(
                        førsteMåned = YearMonth.of(2018, 1),
                        sisteMåned = YearMonth.of(2019, 1)
                    ),
                    inneholderFangstOgFisk = false,
                    periode = 1
                )
            )

        )
    }
}