package no.nav.dagpenger.regel.api.arena.adapter.v2

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internalV2.RegelApiBehovHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiStatusHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiSubsumsjonHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiTimeoutException
import no.nav.dagpenger.regel.api.internalV2.models.Faktum
import no.nav.dagpenger.regel.api.internalV2.models.Inntekt
import no.nav.dagpenger.regel.api.internalV2.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internalV2.models.MinsteinntektResultat
import no.nav.dagpenger.regel.api.internalV2.models.PeriodeResultat
import no.nav.dagpenger.regel.api.internalV2.models.Subsumsjon
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

class MinsteinntektOgPeriodeApiTest {

    private val beregningsdato = LocalDate.of(2019, 2, 10)

    private val minsteinntektPath = "/v2/minsteinntekt"

    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    @Test
    fun `Minsteinntekt and Periode API specification test - Should match json field names and format`() {

        val behovHttpClient: RegelApiBehovHttpClient = mockk()
        val statusHttpClient: RegelApiStatusHttpClient = mockk()
        val subsumsjonHttpClient: RegelApiSubsumsjonHttpClient = mockk()

        every {
            behovHttpClient.run(behovRequest = any())
        } returns "behov/status/123"

        every {
            runBlocking { statusHttpClient.pollStatus("behov/status/123") }
        } returns "subsumsjon/0987"

        every {
            subsumsjonHttpClient.getSubsumsjon("subsumsjon/0987")
        } returns subsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                behovHttpClient = behovHttpClient,
                statusHttpClient = statusHttpClient,
                subsumsjonHttpClient = subsumsjonHttpClient
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                JSONAssert.assertEquals(
                    expectedJson, response.content,
                    CustomComparator(JSONCompareMode.LENIENT,
                        Customization("opprettet") { _, _ -> true },
                        Customization("utfort") { _, _ -> true }))
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for minsteinntekt on uhandled errors`() {

        val behovHttpClient: RegelApiBehovHttpClient = mockk()

        every { runBlocking { behovHttpClient.run(any()) } } throws RuntimeException()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                behovHttpClient = behovHttpClient
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("Uhåndtert feil", problem?.title)
                assertEquals("about:blank", problem?.type.toString())
                assertEquals(500, problem?.status)
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for minsteinntekt on timout errors`() {

        val behovHttpClient: RegelApiBehovHttpClient = mockk()
        val statusHttpClient: RegelApiStatusHttpClient = mockk()

        every {
            behovHttpClient.run(behovRequest = any())
        } returns "behov/status/123"

        every { runBlocking { statusHttpClient.pollStatus(any()) } } throws RegelApiTimeoutException("timeout")

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                behovHttpClient = behovHttpClient,
                statusHttpClient = statusHttpClient
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                assertEquals(HttpStatusCode.GatewayTimeout, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("urn:dp:error:regelberegning:tidsavbrudd", problem?.type.toString())
                assertEquals("Tidsavbrudd ved beregning av regel", problem?.title)
                assertEquals(504, problem?.status)
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for minsteinntekt on bad json request`() {

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                        { "badjson" : "error}
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("Parameteret er ikke gyldig json", problem?.title)
                assertEquals("urn:dp:error:parameter", problem?.type.toString())
                assertEquals(400, problem?.status)
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for minsteinntekt on unmatched json - missing mandatory fields`() {

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                        {  "aktorId": "1234" }
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("Parameteret er ikke gyldig, mangler obligatorisk felt: 'Required value 'vedtakId' missing at \$'", problem?.title)
                assertEquals("urn:dp:error:parameter", problem?.type.toString())
                assertEquals(400, problem?.status)
            }
        }
    }

    @Test
    fun ` Should give 401 - Not authorized if token is missing `() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
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
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("Uautorisert", problem?.title)
                assertEquals("urn:dp:error:uautorisert", problem?.type.toString())
                assertEquals(401, problem?.status)
            }
        }
    }

    private fun subsumsjon(): Subsumsjon {
        return Subsumsjon(
            id = "",
            behovId = "123",
            faktum = Faktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = beregningsdato,
                inntektsId = "13445",
                inntektAvvik = true,
                inntektManueltRedigert = true,
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                bruktInntektsPeriode = InntektsPeriode(
                    førsteMåned = YearMonth.of(2018, 1),
                    sisteMåned = YearMonth.of(2019, 1)
                )
            ),
            minsteinntektResultat = MinsteinntektResultat(
                subsumsjonsId = "12345",
                sporingsId = "",
                oppfyllerMinsteinntekt = true,
                regelIdentifikator = "",
                minsteinntektInntektsPerioder = listOf(
                    Inntekt(
                        inntekt = 4999423,
                        inntektsPeriode = InntektsPeriode(
                            førsteMåned = YearMonth.of(2018, 1),
                            sisteMåned = YearMonth.of(2019, 1)
                        ),
                        andel = 111,
                        inneholderFangstOgFisk = false,
                        periode = 1
                    )
                )
            ),
            periodeResultat = PeriodeResultat(
                subsumsjonsId = "1234",
                sporingsId = "",
                regelIdentifikator = "",
                periodeAntallUker = 104
            ),
            grunnlagResultat = null,
            satsResultat = null
        )
    }

    private val expectedJson =
        """{"minsteinntektSubsumsjonsId":"12345","periodeSubsumsjonsId":"1234","opprettet":"2000-08-11T15:30:11","utfort":"2000-08-11T15:30:11","parametere":{"aktorId":"1234","vedtakId":123,"beregningsdato":"2019-02-10","inntektsId":"13445","harAvtjentVerneplikt":false,"oppfyllerKravTilFangstOgFisk":false,"bruktInntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"}},"resultat":{"oppfyllerKravTilMinsteArbeidsinntekt":true,"periodeAntallUker":104},"inntekt":[{"inntekt":4999423,"periode":1,"inntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"},"inneholderNaeringsinntekter":false,"andel":111}],"inntektManueltRedigert":true,"inntektAvvik":true}"""
}