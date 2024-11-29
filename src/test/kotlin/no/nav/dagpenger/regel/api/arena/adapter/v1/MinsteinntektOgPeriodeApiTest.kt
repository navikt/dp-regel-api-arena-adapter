package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeRegelfaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektRegel
import no.nav.dagpenger.regel.api.internal.BehovRequest
import no.nav.dagpenger.regel.api.internal.RegelApi
import no.nav.dagpenger.regel.api.internal.RegelApiTimeoutException
import no.nav.dagpenger.regel.api.internal.RegelKontekst
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import no.nav.dagpenger.regel.api.serder.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import java.net.URI
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
        val synchronousSubsumsjonClient: RegelApi = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } returns minsteinntektOgPeriodeSubsumsjon()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = synchronousSubsumsjonClient,
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                          "aktorId": "1234",
                          "vedtakId": 5678,
                          "beregningsdato": "2019-02-27",
                          "inntektsId": "12345"
                        }
                        """.trimIndent(),
                    )
                }
            assertEquals(HttpStatusCode.OK, response.status)
            JSONAssert.assertEquals(
                expectedJson,
                response.bodyAsText(),
                CustomComparator(
                    JSONCompareMode.STRICT,
                    Customization("opprettet") { _, _ -> true },
                    Customization("utfort") { _, _ -> true },
                ),
            )
        }
    }

    @Test
    fun `Minsteinntekt og Periode API spesifkasjonstest - Skal ikke inkludere minsteinntektregels resultat hvis det er null`() {
        val synchronousSubsumsjonClient: RegelApi = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } returns minsteinntektOgPeriodeSubsumsjon().copy(resultat = MinsteinntektOgPeriodeResultat(true, 104, null))

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = synchronousSubsumsjonClient,
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                }
            assertEquals(HttpStatusCode.OK, response.status)
            JSONAssert.assertEquals(
                expectedJsonUtenMinsteinntektRegel,
                response.bodyAsText(),
                CustomComparator(
                    JSONCompareMode.STRICT,
                    Customization("opprettet") { _, _ -> true },
                    Customization("utfort") { _, _ -> true },
                ),
            )
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for minsteinntekt ved uhåndterte feil`() {
        val synchronousSubsumsjonClient: RegelApi = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } throws RuntimeException()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = synchronousSubsumsjonClient,
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                }
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe "Uhåndtert feil"
                type shouldBe URI("about:blank")
                status shouldBe 500
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for Subsumsjon med problem`() {
        val problem = Problem(title = "problem")
        val synchronousSubsumsjonClient =
            mockk<RegelApi>().apply {
                coEvery {
                    this@apply.getSubsumsjonSynchronously(
                        any(),
                        any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
                    )
                } throws SubsumsjonProblem(problem)
            }

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = synchronousSubsumsjonClient,
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                }
            assertEquals(HttpStatusCode.BadGateway, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                this shouldBe problem
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for minsteinntekt ved timout errors`() {
        val synchronousSubsumsjonClient: RegelApi = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } throws RegelApiTimeoutException("timeout")

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = synchronousSubsumsjonClient,
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                }
            assertEquals(HttpStatusCode.GatewayTimeout, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe "Tidsavbrudd ved beregning av regel"
                type shouldBe URI("urn:dp:error:regelberegning:tidsavbrudd")
                status shouldBe 504
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 hvis både verneplikt og lærling er true`() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe
                    "Ugyldig kombinasjon av parametere: harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig"
                type shouldBe URI("urn:dp:error:parameter")
                status shouldBe 400
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for dagpengegrunnlag med ugyldig json request`() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        { "badjson" : "error}
                        """.trimIndent(),
                    )
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe
                    "Parameteret er ikke gyldig json"
                type shouldBe URI("urn:dp:error:parameter")
                status shouldBe 400
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for json med manglende obligatoriske felt`() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {  "aktorId": "1234" }
                        """.trimIndent(),
                    )
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe
                    "Parameteret er ikke gyldig json"
                type shouldBe URI("urn:dp:error:parameter")
                status shouldBe 400
            }
        }
    }

    @Test
    fun `Skal svare med 401 hvis request mangler bearer token`() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
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
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe
                    "Uautorisert"
                type shouldBe URI("urn:dp:error:uautorisert")
            }
        }
    }

    @Test
    fun `Skal håndtere request med både regelverk- og beregningsdato`() {
        val synchronousSubsumsjonClient: RegelApi = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> MinsteinntektOgPeriodeSubsumsjon>(),
            )
        } returns minsteinntektOgPeriodeSubsumsjon()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = synchronousSubsumsjonClient,
                )
            }
            val response =
                client.post(minsteinntektPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
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
                }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    private fun minsteinntektOgPeriodeSubsumsjon(): MinsteinntektOgPeriodeSubsumsjon =
        MinsteinntektOgPeriodeSubsumsjon(
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
