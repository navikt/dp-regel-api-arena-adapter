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
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.SynchronousPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.MinsteinntektFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.MinsteinntektResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.PeriodeFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.PeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.PeriodeSubsumsjon
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class MinsteinntektOgPeriodeApiSpec() {
    val beregningsdato = LocalDate.of(2019, 2, 10)
    val localDateTime = LocalDateTime.of(2000, 8, 11, 15, 30, 11)

    @Test
    fun `Minsteinntekt and Periode API specification test - Should match json field names and formats`() {

        val synchronousMinsteinntekt: SynchronousMinsteinntekt = mockk()
        val synchronousPeriode: SynchronousPeriode = mockk()

        every { runBlocking { synchronousMinsteinntekt.getMinsteinntektSynchronously(parametere = any()) } } returns minsteinntektSubsumsjon()
        every { runBlocking { synchronousPeriode.getPeriodeSynchronously(parametere = any()) } } returns periodeSubsumsjon()

        withTestApplication({
            regelApiAdapter(
                synchronousMinsteinntekt,
                synchronousPeriode,
                mockk(),
                mockk()
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/minsteinntekt") {
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
        """{"minsteinntektSubsumsjonsId":"12345","periodeSubsumsjonsId":"1234","opprettet":"2000-08-11T15:30:11","utfort":"2000-08-11T15:30:11","parametere":{"aktorId":"1234","vedtakId":123,"beregningsdato":"2019-02-10","inntektsId":"13445","harAvtjentVerneplikt":false,"oppfyllerKravTilFangstOgFisk":false,"bruktInntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"}},"resultat":{"oppfyllerKravTilMinsteArbeidsinntekt":true,"periodeAntallUker":104},"inntekt":[{"inntekt":4999423,"periode":1,"inntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"},"inneholderNaeringsinntekter":false,"andel":111}]}"""

    private fun periodeSubsumsjon(): PeriodeSubsumsjon {

        return PeriodeSubsumsjon(
            subsumsjonsId = "1234",
            opprettet = localDateTime,
            utfort = localDateTime,
            faktum = PeriodeFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = beregningsdato,
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                bruktInntektsPeriode = InntektsPeriode(
                    foersteMaaned = YearMonth.of(2018, 1),
                    sisteMaaned = YearMonth.of(2019, 1)
                )

            ),
            resultat = PeriodeResultat(104),
            inntekt = setOf(
                Inntekt(
                    inntekt = 4999423,
                    inntektsPeriode = InntektsPeriode(
                        foersteMaaned = YearMonth.of(2018, 1),
                        sisteMaaned = YearMonth.of(2019, 1)
                    ),
                    andel = 111,
                    inneholderFangstOgFisk = false,
                    periode = 1
                )
            )

        )
    }

    private fun minsteinntektSubsumsjon(): MinsteinntektSubsumsjon {

        return MinsteinntektSubsumsjon(
            subsumsjonsId = "12345",
            opprettet = localDateTime,
            utfort = localDateTime,
            faktum = MinsteinntektFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = beregningsdato,
                inntektsId = "13445",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                bruktInntektsPeriode = InntektsPeriode(
                    foersteMaaned = YearMonth.of(2018, 1),
                    sisteMaaned = YearMonth.of(2019, 1)
                )

            ),
            resultat = MinsteinntektResultat(
                oppfyllerKravTilMinsteArbeidsinntekt = true
            ),
            inntekt = listOf(
                Inntekt(
                    inntekt = 4999423,
                    inntektsPeriode = InntektsPeriode(
                        foersteMaaned = YearMonth.of(2018, 1),
                        sisteMaaned = YearMonth.of(2019, 1)
                    ),
                    andel = 111,
                    inneholderFangstOgFisk = false,
                    periode = 1
                )
            )
        )
    }
}