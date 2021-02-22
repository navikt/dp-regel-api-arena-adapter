package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
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
    fun ` Map parameters to RehovRequest`() {
        val parametere = GrunnlagOgSatsParametere(
            aktorId = "12345",
            vedtakId = 123,
            beregningsdato = LocalDate.of(2019, 5, 13),
            harAvtjentVerneplikt = true,
            oppfyllerKravTilFangstOgFisk = false,
            grunnlag = 3000,
            antallBarn = 3,
            oppfyllerKravTilLaerling = false,
            virkningstidspunkt = LocalDate.of(2020, 6, 14)
        )

        val expectedBehovRequest = BehovRequest(
            aktorId = "12345",
            vedtakId = 123,
            beregningsdato = LocalDate.of(2019, 5, 13),
            harAvtjentVerneplikt = true,
            oppfyllerKravTilFangstOgFisk = false,
            manueltGrunnlag = 3000,
            antallBarn = 3,
            lærling = false
        )

        val result = behovFromParametere(parametere)

        assertEquals(expectedBehovRequest, result)
    }

    @Test
    fun `Grunnlag and Sats API specification test - Should match json field names and format`() {

        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } returns grunnlagOgSatsSubsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
                      "oppfyllerKravTilLaerling": true
                    }
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                JSONAssert.assertEquals(
                    expectedJson,
                    response.content,
                    CustomComparator(
                        JSONCompareMode.STRICT,
                        Customization("opprettet") { _, _ -> true },
                        Customization("utfort") { _, _ -> true }
                    )
                )
            }
        }
    }

    @Test
    fun `Grunnlag and Sats API specification test - Should match json field names and format for v2 with beregningsregel in sats`() {

        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            runBlocking {
                synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                    any(),
                    any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
                )
            }
        } returns grunnlagOgSatsSubsumsjonWithSatsBeregningsregel()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
                    expectedJsonWithBeregningsregel,
                    response.content,
                    CustomComparator(
                        JSONCompareMode.STRICT,
                        Customization("opprettet") { _, _ -> true },
                        Customization("utfort") { _, _ -> true }
                    )
                )
            }
        }
    }

    @Test
    fun `Grunnlag and Sats re-beregning API specification test - Should match json field names and format`() {

        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } returns grunnlagOgSatsSubsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, "$dagpengegrunnlagPath-reberegning") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun `Grunnlag and Sats beregning should give problem when grunnlag result is negative`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } throws NegativtGrunnlagException("Negativt grunnlag")

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, "$dagpengegrunnlagPath-reberegning") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!).apply {
                    this?.type shouldBe URI("urn:dp:error:regelberegning:grunnlag:negativ")
                }
            }
        }
    }

    @Test
    fun `Grunnlag and Sats beregning should give problem when grunnlag result is 0`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } throws NullGrunnlagException("Negativt grunnlag")

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, "$dagpengegrunnlagPath-reberegning") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!).apply {
                    this?.type shouldBe URI("urn:dp:error:regelberegning:grunnlag:0")
                }
            }
        }
    }

    @Test
    fun `Grunnlag and Sats re-beregning API should give 400 on illegal inntektsId`() {

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = mockk()
            )
        }) {
            handleRequest(HttpMethod.Post, "$dagpengegrunnlagPath-reberegning") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!).apply {
                    this?.type shouldBe URI("urn:dp:error:parameter")
                }
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for dagpengegrunnlag on uhandled errors`() {

        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } throws RuntimeException()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
    fun ` Should give API errors as HTTP problems rfc7807 for dagpengegrunnlag on Subsumsjon Problem`() {

        val problem = Problem(title = "subsumsjon problem")
        val synchronousSubsumsjonClient = mockk<SynchronousSubsumsjonClient>().apply {
            coEvery {
                this@apply.getSubsumsjonSynchronously(
                    any(),
                    any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
                )
            } throws SubsumsjonProblem(problem)
        }

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
                assertEquals(HttpStatusCode.BadGateway, response.status())
                moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!).apply {
                    this shouldBe problem
                }
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for dagpengegrunnlag on timout errors`() {

        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } throws RegelApiTimeoutException("timeout")

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
    fun ` Should give API errors as HTTP problems rfc7807 for dagpengegrunnlag on bad json request`() {

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
    fun ` Should give API errors as HTTP problems rfc7807 for dagpengegrunnlag on unmatched json - missing mandatory fields`() {

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
                assertEquals(
                    "Parameteret er ikke gyldig, mangler obligatorisk felt: 'Required value 'vedtakId' missing at \$'",
                    problem?.title
                )
                assertEquals("urn:dp:error:parameter", problem?.type.toString())
                assertEquals(400, problem?.status)
            }
        }
    }

    @Test
    fun ` Should give API errors as HTTP problems rfc7807 for grunnlag on invalid combination of verneplikt and lærling`() {

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider()
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val problem = moshiInstance.adapter<Problem>(Problem::class.java).fromJson(response.content!!)
                assertEquals("Ugyldig kombinasjon av parametere: harAvtjentVerneplikt og oppfyllerKravTilLaerling kan ikke vaere true samtidig", problem?.title)
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
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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

    @Test
    fun `med virkningstidspunkt og beregningsdato`() {
        val synchronousSubsumsjonClient: SynchronousSubsumsjonClient = mockk()

        coEvery {
            synchronousSubsumsjonClient.getSubsumsjonSynchronously(
                any(),
                any<(Subsumsjon, LocalDateTime, LocalDateTime) -> GrunnlagOgSatsSubsumsjon>()
            )
        } returns grunnlagOgSatsSubsumsjon()

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                synchronousSubsumsjonClient = synchronousSubsumsjonClient
            )
        }) {
            handleRequest(HttpMethod.Post, dagpengegrunnlagPath) {
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
                      "virkningstidspunkt": "2020-03-28"
                    }

                    """.trimIndent()
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun grunnlagOgSatsSubsumsjon(): GrunnlagOgSatsSubsumsjon {
        return GrunnlagOgSatsSubsumsjon(
            grunnlagSubsumsjonsId = "1234",
            satsSubsumsjonsId = "4567",
            opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            parametere = GrunnlagOgSatsRegelFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 2, 10),
                inntektsId = "1234",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                antallBarn = 0,
                grunnlag = 12345
            ),
            resultat = GrunnlagOgSatsResultat(
                grunnlag = Grunnlag(
                    avkortet = 12345,
                    uavkortet = 12345
                    // beregningsregel = GrunnlagBeregningsregel.ORDINAER_ETTAAR
                ),
                sats = Sats(124, 234),
                beregningsRegel = GrunnlagBeregningsregel.ORDINAER_ETTAAR,
                benyttet90ProsentRegel = false
            ),
            inntekt =
                setOf(
                    no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                        inntekt = 4999423,
                        inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                            foersteMaaned = YearMonth.of(2018, 1),
                            sisteMaaned = YearMonth.of(2019, 1)
                        ),
                        inneholderNaeringsinntekter = false,
                        periode = 1
                    )
                ),
            inntektManueltRedigert = true,
            inntektAvvik = true
        )
    }

    private fun grunnlagOgSatsSubsumsjonWithSatsBeregningsregel(): GrunnlagOgSatsSubsumsjon {
        return GrunnlagOgSatsSubsumsjon(
            grunnlagSubsumsjonsId = "1234",
            satsSubsumsjonsId = "4567",
            opprettet = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            utfort = LocalDateTime.of(2019, 4, 25, 1, 1, 1),
            parametere = GrunnlagOgSatsRegelFaktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 2, 10),
                inntektsId = "1234",
                harAvtjentVerneplikt = false,
                oppfyllerKravTilFangstOgFisk = false,
                antallBarn = 0,
                grunnlag = 12345
            ),
            resultat = GrunnlagOgSatsResultat(
                grunnlag = Grunnlag(
                    avkortet = 12345,
                    uavkortet = 12345,
                    beregningsregel = GrunnlagBeregningsregel.ORDINAER_ETTAAR
                ),
                sats = Sats(124, 234, beregningsregel = SatsBeregningsregel.ORDINAER),
                benyttet90ProsentRegel = false
            ),
            inntekt =
                setOf(
                    no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt(
                        inntekt = 4999423,
                        inntektsPeriode = no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode(
                            foersteMaaned = YearMonth.of(2018, 1),
                            sisteMaaned = YearMonth.of(2019, 1)
                        ),
                        inneholderNaeringsinntekter = false,
                        periode = 1
                    )
                ),
            inntektManueltRedigert = true,
            inntektAvvik = true
        )
    }

    private val expectedJson =
        """{"grunnlagSubsumsjonsId":"1234","satsSubsumsjonsId":"4567","opprettet":"2000-08-11T15:30:11","utfort":"2000-08-11T15:30:11","parametere":{"aktorId":"1234","vedtakId":123,"beregningsdato":"2019-02-10","inntektsId":"1234","harAvtjentVerneplikt":false,"oppfyllerKravTilFangstOgFisk":false,"antallBarn":0,"grunnlag":12345},"resultat":{"grunnlag":{"avkortet":12345,"uavkortet":12345},"sats":{"dagsats":124,"ukesats":234},"beregningsRegel":"ORDINAER_ETTAAR","benyttet90ProsentRegel":false},"inntekt":[{"inntekt":4999423,"periode":1,"inntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"},"inneholderNaeringsinntekter":false}],"inntektManueltRedigert":true,"inntektAvvik":true}"""

    private val expectedJsonWithBeregningsregel =
        """{"grunnlagSubsumsjonsId":"1234","satsSubsumsjonsId":"4567","opprettet":"2000-08-11T15:30:11","utfort":"2000-08-11T15:30:11","parametere":{"aktorId":"1234","vedtakId":123,"beregningsdato":"2019-02-10","inntektsId":"1234","harAvtjentVerneplikt":false,"oppfyllerKravTilFangstOgFisk":false,"antallBarn":0,"grunnlag":12345},"resultat":{"grunnlag":{"avkortet":12345,"uavkortet":12345,"beregningsregel":"ORDINAER_ETTAAR"},"sats":{"dagsats":124,"ukesats":234,"beregningsregel":"ORDINAER"},"benyttet90ProsentRegel":false},"inntekt":[{"inntekt":4999423,"periode":1,"inntektsPeriode":{"foersteMaaned":"2018-01","sisteMaaned":"2019-01"},"inneholderNaeringsinntekter":false}],"inntektManueltRedigert":true,"inntektAvvik":true}"""
}
