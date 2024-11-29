package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
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
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag.expectedGrunnlagJson
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag.expectedGrunnlagJsonWithBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.SatsBeregningsregel
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
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals

class GrunnlagOgSatsApiTest {
    private val dagpengegrunnlagPath = "/v1/dagpengegrunnlag"
    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    @Test
    fun `Mapper parametere til BehovRequest`() {
        val parametere =
            GrunnlagOgSatsParametere(
                aktorId = "12345",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 5, 13),
                harAvtjentVerneplikt = true,
                oppfyllerKravTilFangstOgFisk = false,
                manueltGrunnlag = 3000,
                forrigeGrunnlag = 7000,
                antallBarn = 3,
                oppfyllerKravTilLaerling = false,
            )
        val parametreMedRegelverksdato = parametere.copy(regelverksdato = LocalDate.of(2020, 6, 14))
        val standardBehovRequest =
            BehovRequest(
                aktorId = "12345",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 5, 13),
                harAvtjentVerneplikt = true,
                oppfyllerKravTilFangstOgFisk = false,
                manueltGrunnlag = 3000,
                forrigeGrunnlag = 7000,
                antallBarn = 3,
                lærling = false,
                regelverksdato = LocalDate.of(2019, 5, 13),
            )
        val behovRequestMedRegelverksdato = standardBehovRequest.copy(regelverksdato = LocalDate.of(2020, 6, 14))

        assertEquals(standardBehovRequest, behovFromParametere(parametere))
        assertEquals(behovRequestMedRegelverksdato, behovFromParametere(parametreMedRegelverksdato))
    }

    @Test
    fun `Deprecated grunnlag mappes til manueltgrunnlag`() {
        val parametere =
            GrunnlagOgSatsParametere(
                aktorId = "12345",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 5, 13),
                grunnlag = 4000,
            )
        val standardBehovRequest =
            BehovRequest(
                aktorId = "12345",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 5, 13),
                regelverksdato = LocalDate.of(2019, 5, 13),
                manueltGrunnlag = 4000,
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                lærling = false,
                antallBarn = 0,
            )
        assertEquals(standardBehovRequest, behovFromParametere(parametere))
    }

    @Test
    fun `Grunnlag and Sats API spesifikasjonstest - håndterer json korrekt`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } returns grunnlagOgSatsSubsumsjon()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
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
                expectedGrunnlagJson,
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
    fun `Grunnlag og Sats API spesifikasjonstest - håndterer json body korrekt for v2 med beregningsregel i sats`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            runBlocking {
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                    any(),
                    any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
                )
            }
        } returns grunnlagOgSatsSubsumsjonWithSatsBeregningsregel()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
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
                expectedGrunnlagJsonWithBeregningsregel,
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
    fun `Grunnlag og Sats re-beregning API spesifikasjonstest - håndterer json korrekt`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } returns grunnlagOgSatsSubsumsjon()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post("$dagpengegrunnlagPath-reberegning") {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "${ULID().nextULID()}",
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `Grunnlag og Sats beregning skal svare med http-problem når resultatet er negativt`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } throws NegativtGrunnlagException("Negativt grunnlag")

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post("$dagpengegrunnlagPath-reberegning") {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "${ULID().nextULID()}",
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.InternalServerError
            moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this?.type shouldBe URI("urn:dp:error:regelberegning:grunnlag:negativ")
            }
        }
    }

    @Test
    fun `Grunnlag og Sats beregning skal svare med http-problem når resultatet er 0`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } throws NullGrunnlagException("Negativt grunnlag")

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post("$dagpengegrunnlagPath-reberegning") {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "${ULID().nextULID()}",
                      "beregningsdato": "2019-02-27",
                      "lærling": true,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.InternalServerError
            moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this?.type shouldBe URI("urn:dp:error:regelberegning:grunnlag:0")
            }
        }
    }

    @Test
    fun `Grunnlag og Sats re-beregning API skal svare med 400 for ugyldig inntektsId`() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = mockk(),
                )
            }
            val response = client.post("$dagpengegrunnlagPath-reberegning") {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "bla bla",
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.BadRequest
            moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this?.type shouldBe URI("urn:dp:error:parameter")
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for uhåndtert feil`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } throws RuntimeException()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "${ULID().nextULID()}",
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.InternalServerError
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this.shouldNotBeNull()
                this.type shouldBe URI("about:blank")
                this.status shouldBe 500
                this.title shouldBe "Uhåndtert feil"
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for SubsumsjonProblem`() {
        val problem = Problem(title = "subsumsjon problem")
        val synchronousSubsumsjonClient =
            mockk<SynchronousSubsumsjonClient>().apply {
                coEvery {
                    this@apply.getSubsumsjonSynchronously(
                        any(),
                        any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
                    )
                } throws SubsumsjonProblem(problem)
            }

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "${ULID().nextULID()}",
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.BadGateway
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this shouldBe problem
            }
        }
    }

    @Test
    fun `Skal svare med HTTP problem rfc7807 for timeout errors`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } throws RegelApiTimeoutException("timeout")

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "inntektsId" : "${ULID().nextULID()}",
                      "beregningsdato": "2019-02-27",
                      "harAvtjentVerneplikt": false,
                      "oppfyllerKravTilFangstOgFisk": false
                    }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.GatewayTimeout
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this.shouldNotBeNull()
                this.type shouldBe URI("urn:dp:error:regelberegning:tidsavbrudd")
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
            val response = client.post(dagpengegrunnlagPath) {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    { "badjson" : "error}
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.BadRequest
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this.shouldNotBeNull()
                this.type shouldBe URI("urn:dp:error:parameter")
                this.status shouldBe 400
                this.title shouldBe "Parameteret er ikke gyldig json"
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
            val response = client.post(dagpengegrunnlagPath) {
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {  "aktorId": "1234" }
                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.BadRequest
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this.shouldNotBeNull()
                this.type shouldBe URI("urn:dp:error:parameter")
                this.status shouldBe 400
                this.title shouldBe "Parameteret er ikke gyldig json"
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
            val response = client.post(dagpengegrunnlagPath) {
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
            response.status shouldBe HttpStatusCode.BadRequest
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this.shouldNotBeNull()
                assertEquals(
                    "Ugyldig kombinasjon av parametere: harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig",
                    title,
                )
                assertEquals("urn:dp:error:parameter", type.toString())
                assertEquals(400, status)
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
            val response = client.post(dagpengegrunnlagPath) {
                header(HttpHeaders.ContentType, "application/json")
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
            response.status shouldBe HttpStatusCode.Unauthorized
            moshiInstance.adapter(Problem::class.java).fromJson(response.bodyAsText()).apply {
                this.shouldNotBeNull()
                assertEquals("Uautorisert", title)
                assertEquals("urn:dp:error:uautorisert", type.toString())
                assertEquals(401, status)
            }
        }
    }

    @Test
    fun `Skal håndtere request med både regelverk- og beregningsdato`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } returns grunnlagOgSatsSubsumsjon()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
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
            response.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `Skal håndtere request med manuelt grunnlag`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } returns grunnlagOgSatsSubsumsjon()
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
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
                      "regelverksdato": "2020-03-28",
                      "manueltGrunnlag": 1000000
                    }

                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `Skal håndtere request med forrige grunnlag`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>(),
            )
        } returns grunnlagOgSatsSubsumsjon()

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    synchronousSubsumsjonClient = synchronousSubsumsjonClient,
                )
            }
            val response = client.post(dagpengegrunnlagPath) {
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
                      "regelverksdato": "2020-03-28",
                      "forrigeGrunnlag": 600000
                    }

                    """.trimIndent(),
                )
            }
            response.status shouldBe HttpStatusCode.OK
        }
    }

    private fun grunnlagOgSatsSubsumsjon(): GrunnlagOgSatsSubsumsjon {
        return GrunnlagOgSatsSubsumsjon(
            grunnlagSubsumsjonsId = "1234",
            satsSubsumsjonsId = "4567",
            opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            parametere =
            GrunnlagOgSatsRegelFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 2, 10),
                inntektsId = "1234",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                antallBarn = 0,
                grunnlag = 12345,
                manueltGrunnlag = 12345,
                forrigeGrunnlag = null,
            ),
            resultat =
            GrunnlagOgSatsResultat(
                grunnlag =
                Grunnlag(
                    avkortet = 12345,
                    uavkortet = 12345,
                    beregningsregel = GrunnlagBeregningsregel.ORDINAER_ETTAAR,
                ),
                sats = Sats(124, 234),
                benyttet90ProsentRegel = false,
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
                    inneholderNaeringsinntekter = false,
                    periode = 1,
                ),
            ),
            inntektManueltRedigert = true,
            inntektAvvik = true,
        )
    }

    private fun grunnlagOgSatsSubsumsjonWithSatsBeregningsregel(): GrunnlagOgSatsSubsumsjon {
        return GrunnlagOgSatsSubsumsjon(
            grunnlagSubsumsjonsId = "1234",
            satsSubsumsjonsId = "4567",
            opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            parametere =
            GrunnlagOgSatsRegelFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 2, 10),
                inntektsId = "1234",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                antallBarn = 0,
                grunnlag = 12345,
                manueltGrunnlag = 12345,
                forrigeGrunnlag = null,
            ),
            resultat =
            GrunnlagOgSatsResultat(
                grunnlag =
                Grunnlag(
                    avkortet = 12345,
                    uavkortet = 12345,
                    beregningsregel = GrunnlagBeregningsregel.ORDINAER_ETTAAR,
                ),
                sats = Sats(124, 234, beregningsregel = SatsBeregningsregel.ORDINAER),
                benyttet90ProsentRegel = false,
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
                    inneholderNaeringsinntekter = false,
                    periode = 1,
                ),
            ),
            inntektManueltRedigert = true,
            inntektAvvik = true,
        )
    }
}
