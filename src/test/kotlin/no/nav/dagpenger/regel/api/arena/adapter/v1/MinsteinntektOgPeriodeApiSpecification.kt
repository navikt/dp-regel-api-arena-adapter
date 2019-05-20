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
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.internal.models.InntektMinsteinntekt
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektFaktum
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektResultat
import no.nav.dagpenger.regel.api.internal.models.MinsteinntektSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.PeriodeFaktum
import no.nav.dagpenger.regel.api.internal.models.PeriodeResultat
import no.nav.dagpenger.regel.api.internal.models.PeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.periode.SynchronousPeriode
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class MinsteinntektOgPeriodeApiSpecification {

    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    private val minsteinntektPath: String = "/v1/minsteinntekt"
    private val beregningsdato = LocalDate.of(2019, 2, 10)
    private val localDateTime = LocalDateTime.of(2000, 8, 11, 15, 30, 11)

    private val validjson = """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false,
                      "bruktInntektsPeriode": {
                            "foersteMaaned": "2018-01",
                            "sisteMaaned": "2019-01"
                       }
                    }
                """.trimIndent()

    @Test
    fun `Minsteinntekt and Periode API specification test - should validate bruktInntektsPeriode`() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            runBlocking {
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
                      "oppfyllerKravTilFangstOgFisk": false,
                      "bruktInntektsPeriode": {
                            "foersteMaaned": "2019-01",
                            "sisteMaaned": "2018-01"
                       }
                    }
                """.trimIndent()
                    )
                }
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("urn:dp:error:parameter", problem?.type.toString())
                assertEquals(400, problem?.status)
                assertEquals(
                    "Feil bruktInntektsPeriode: foersteMaaned=2019-01 er etter sisteMaaned=2018-01",
                    problem?.title
                )
            }
        }
    }

    @Test
    fun `Minsteinntekt and Periode API specification test - Should match json field names and formats`() {

        val synchronousMinsteinntekt: SynchronousMinsteinntekt = mockk()
        val synchronousPeriode: SynchronousPeriode = mockk()

        every { runBlocking { synchronousMinsteinntekt.getMinsteinntektSynchronously(parametere = any()) } } returns minsteinntektSubsumsjon()
        every { runBlocking { synchronousPeriode.getPeriodeSynchronously(parametere = any()) } } returns periodeSubsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousMinsteinntekt = synchronousMinsteinntekt,
                synchronousPeriode = synchronousPeriode
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    validjson
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expectedJson, response.content)
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for minsteinntekt on uhandled errors`() {

        val synchronousMinsteinntekt: SynchronousMinsteinntekt = mockk()
        val synchronousPeriode: SynchronousPeriode = mockk()

        every { runBlocking { synchronousMinsteinntekt.getMinsteinntektSynchronously(parametere = any()) } } throws RuntimeException()
        every { runBlocking { synchronousPeriode.getPeriodeSynchronously(parametere = any()) } } throws RuntimeException()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousMinsteinntekt = synchronousMinsteinntekt,
                synchronousPeriode = synchronousPeriode
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    validjson
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

    private val expectedJson =
        """{"minsteinntektSubsumsjonsId":"12345","periodeSubsumsjonsId":"1234","opprettet":"2000-08-11T15:30:11","utfort":"2000-08-11T15:30:11","parametere":{"aktorId":"1234","vedtakId":123,"beregningsdato":"2019-02-10","inntektsId":"13445","harAvtjentVerneplikt":false,"oppfyllerKravTilFangstOgFisk":false,"bruktInntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"}},"resultat":{"oppfyllerKravTilMinsteArbeidsinntekt":true,"periodeAntallUker":104},"inntekt":[{"inntekt":4999423,"periode":1,"inntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"},"inneholderNaeringsinntekter":false,"andel":111}],"inntektManueltRedigert":true,"inntektAvvik":true}"""

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
                    førsteMåned = YearMonth.of(2018, 1),
                    sisteMåned = YearMonth.of(2019, 1)
                )

            ),
            resultat = PeriodeResultat(104)
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
                    førsteMåned = YearMonth.of(2018, 1),
                    sisteMåned = YearMonth.of(2019, 1)
                ),
                inntektManueltRedigert = true,
                inntektAvvik = true

            ),
            resultat = MinsteinntektResultat(
                oppfyllerKravTilMinsteArbeidsinntekt = true
            ),
            inntekt = setOf(
                InntektMinsteinntekt(
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
        )
    }
}