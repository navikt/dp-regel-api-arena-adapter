package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektRegel
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.RegelApiTimeoutException
import no.nav.dagpenger.regel.api.internal.RegelKontekst
import no.nav.dagpenger.regel.api.internal.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class MinsteinntektOgPeriodeApiTest {
    private val minsteinntektPath = "/v1/minsteinntekt"
    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    @Test
    fun `Skal mappe parametre til BehovRequest`() {
        val parametere =
            MinsteinntektOgPeriodeParametere(
                aktorId = "12345",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 5, 13),
                harAvtjentVerneplikt = true,
                oppfyllerKravTilFangstOgFisk = false,
                bruktInntektsPeriode = InntektsPeriode(YearMonth.of(2019, 4), YearMonth.of(2019, 7)),
                oppfyllerKravTilLaerling = false,
            )
        val parametereMedRegelverksdato = parametere.copy(regelverksdato = LocalDate.of(2020, 6, 14))
        val standardBehovRequest =
            BehovRequest(
                aktorId = "12345",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 5, 13),
                harAvtjentVerneplikt = true,
                oppfyllerKravTilFangstOgFisk = false,
                bruktInntektsPeriode =
                no.nav.dagpenger.regel.api.internal.models.InntektsPeriode(
                    YearMonth.of(2019, 4),
                    YearMonth.of(2019, 7),
                ),
                lærling = false,
            )
        val behovRequestMedRegelverksdato = standardBehovRequest.copy(regelverksdato = LocalDate.of(2020, 6, 14))

        assertEquals(standardBehovRequest, behovFromParametere(parametere))
        assertEquals(behovRequestMedRegelverksdato, behovFromParametere(parametereMedRegelverksdato))
    }

    @Test
    fun `Minsteinntekt og Periode API spesifkasjonstest - Skal håndtere json riktig`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } returns minsteinntektOgPeriodeSubsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient,
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
                    """.trimIndent(),
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                JSONAssert.assertEquals(
                    expectedJson,
                    response.content,
                    CustomComparator(
                        JSONCompareMode.STRICT,
                        Customization("opprettet") { _, _ -> true },
                        Customization("utfort") { _, _ -> true },
                    ),
                )
            }
        }
    }

    @Test
    fun `Minsteinntekt og Periode API spesifkasjonstest - Skal ikke inkludere minsteinntektregels resultat hvis det er null`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } returns minsteinntektOgPeriodeSubsumsjon().copy(resultat = MinsteinntektOgPeriodeResultat(true, 104, null))

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient,
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
                    """.trimIndent(),
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                JSONAssert.assertEquals(
                    expectedJsonUtenMinsteinntektRegel,
                    response.content,
                    CustomComparator(
                        JSONCompareMode.STRICT,
                        Customization("opprettet") { _, _ -> true },
                        Customization("utfort") { _, _ -> true },
                    ),
                )
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for minsteinntekt ved uhåndterte feil`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } throws RuntimeException()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient,
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

                    """.trimIndent(),
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
    fun `Skal svare med HTTP problem rfc7807 for Subsumsjon med problem`() {
        val problem = Problem(title = "problem")
        val synchronousSubsumsjonClient =
            mockk<SynchronousSubsumsjonClient>().apply {
                coEvery {
                    this@apply.getSubsumsjonSynchronously(
                        any(),
                        any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
                    )
                } throws SubsumsjonProblem(problem)
            }

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient,
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

                    """.trimIndent(),
                )
            }.apply {
                assertEquals(HttpStatusCode.BadGateway, response.status())
                moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!).apply {
                    this@apply shouldBe problem
                }
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for minsteinntekt ved timout errors`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } throws RegelApiTimeoutException("timeout")

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient,
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

                    """.trimIndent(),
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
    fun `Skal svare med HTTP problem rfc7807 hvis både verneplikt og lærling er true`() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
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
                      "harAvtjentVerneplikt": true,
                      "oppfyllerKravTilFangstOgFisk": false,
                      "oppfyllerKravTilLaerling": true
                    }
                    """.trimIndent(),
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals(
                    "Ugyldig kombinasjon av parametere: harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig",
                    problem?.title,
                )
                assertEquals("urn:dp:error:parameter", problem?.type.toString())
                assertEquals(400, problem?.status)
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for dagpengegrunnlag med ugyldig json request`() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    { "badjson" : "error}
                    """.trimIndent(),
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
    fun `Skal svare med HTTP problem rfc7807 for json med manglende obligatoriske felt`() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
            )
        }) {
            handleRequest(HttpMethod.Post, minsteinntektPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {  "aktorId": "1234" }
                    """.trimIndent(),
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals(
                    "Parameteret er ikke gyldig json",
                    problem?.title,
                )
                assertEquals("urn:dp:error:parameter", problem?.type.toString())
                assertEquals(400, problem?.status)
            }
        }
    }

    @Test
    fun `Skal svare med 401 hvis request mangler bearer token`() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
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

                    """.trimIndent(),
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

    @Test
    fun `Skal håndtere request med både regelverk- og beregningsdato`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } returns minsteinntektOgPeriodeSubsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient,
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
                      "oppfyllerKravTilFangstOgFisk": false,
                      "regelverksdato": "2020-03-28"
                    }
                    """.trimIndent(),
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun minsteinntektOgPeriodeSubsumsjon(): MinsteinntektOgPeriodeSubsumsjon {
        return MinsteinntektOgPeriodeSubsumsjon(
            minsteinntektSubsumsjonsId = "12345",
            periodeSubsumsjonsId = "1234",
            opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            parametere =
            MinsteinntektOgPeriodeRegelfaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 2, 10),
                inntektsId = "13445",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                bruktInntektsPeriode =
                no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                    foersteMaaned = YearMonth.of(2018, 1),
                    sisteMaaned = YearMonth.of(2019, 1),
                ),
            ),
            resultat =
            MinsteinntektOgPeriodeResultat(
                oppfyllerKravTilMinsteArbeidsinntekt = true,
                periodeAntallUker = 104,
                minsteinntektRegel = MinsteinntektRegel.ORDINAER,
            ),
            inntekt =
            setOf(
                no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                    inntekt = 4999423,
                    inntektsPeriode =
                    no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                        foersteMaaned = YearMonth.of(2018, 1),
                        sisteMaaned = YearMonth.of(2019, 1),
                    ),
                    andel = 111,
                    inneholderNaeringsinntekter = false,
                    periode = 1,
                ),
            ),
            inntektManueltRedigert = true,
            inntektAvvik = true,
        )
    }

    private val expectedJson =
        """{
  "minsteinntektSubsumsjonsId": "12345",
  "periodeSubsumsjonsId": "1234",
  "opprettet": "2000-08-11T15:30:11",
  "utfort": "2000-08-11T15:30:11",
  "parametere": {
    "aktorId": "1234",
    "vedtakId": 123,
    "beregningsdato": "2019-02-10",
    "inntektsId": "13445",
    "harAvtjentVerneplikt": false,
    "oppfyllerKravTilFangstOgFisk": false,
    "oppfyllerKravTilLaerling": false,
    "bruktInntektsPeriode": {
      "foersteMaaned": "2018-01",
      "sisteMaaned": "2019-01"
    }
  },
  "resultat": {
    "oppfyllerKravTilMinsteArbeidsinntekt": true,
    "periodeAntallUker": 104,
    "minsteinntektRegel": "ORDINAER"
  },
  "inntekt": [
    {
      "inntekt": 4999423,
      "periode": 1,
      "inntektsPeriode": {
        "foersteMaaned": "2018-01",
        "sisteMaaned": "2019-01"
      },
      "inneholderNaeringsinntekter": false,
      "andel": 111
    }
  ],
  "inntektManueltRedigert": true,
  "inntektAvvik": true
}"""
    private val expectedJsonUtenMinsteinntektRegel =
        """{
  "minsteinntektSubsumsjonsId": "12345",
  "periodeSubsumsjonsId": "1234",
  "opprettet": "2000-08-11T15:30:11",
  "utfort": "2000-08-11T15:30:11",
  "parametere": {
    "aktorId": "1234",
    "vedtakId": 123,
    "beregningsdato": "2019-02-10",
    "inntektsId": "13445",
    "harAvtjentVerneplikt": false,
    "oppfyllerKravTilFangstOgFisk": false,
    "oppfyllerKravTilLaerling": false,
    "bruktInntektsPeriode": {
      "foersteMaaned": "2018-01",
      "sisteMaaned": "2019-01"
    }
  },
  "resultat": {
    "oppfyllerKravTilMinsteArbeidsinntekt": true,
    "periodeAntallUker": 104
  },
  "inntekt": [
    {
      "inntekt": 4999423,
      "periode": 1,
      "inntektsPeriode": {
        "foersteMaaned": "2018-01",
        "sisteMaaned": "2019-01"
      },
      "inneholderNaeringsinntekter": false,
      "andel": 111
    }
  ],
  "inntektManueltRedigert": true,
  "inntektAvvik": true
}"""
}
