package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.kotest.assertions.json.shouldEqualJson
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
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat
import no.nav.dagpenger.regel.api.serder.jacksonObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InntjeningsperiodeApiSpec {
    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    private val inntjeningsperiodePath = "/v1/inntjeningsperiode"

    @Test
    fun `Inntjeningsperiode API specification test - Should match json field names and formats`() {
        val inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient = mockk()

        coEvery {
            inntektApiBeregningsdatoHttpClient.getInntjeningsperiode(any())
        } returns
            InntjeningsperiodeResultat(
                true,
                InntjeningsperiodeParametre(
                    "1234",
                    5678,
                    "2019-02-27",
                    "12345",
                ),
            )

        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    inntektApiBeregningsdatoHttpClient = inntektApiBeregningsdatoHttpClient,
                )
            }
            val response =
                client.post(inntjeningsperiodePath) {
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
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().shouldEqualJson(expectedJson)
        }
    }

    @Test
    fun ` Should give 401 - Not authorized if token is missing `() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                )
            }
            val response =
                client.post(inntjeningsperiodePath) {
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
            response.status shouldBe HttpStatusCode.Unauthorized
            val problem = jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)
            assertEquals("Uautorisert", problem?.title)
            assertEquals("urn:dp:error:uautorisert", problem?.type.toString())
            assertEquals(401, problem?.status)
        }
    }

    private val expectedJson =
        """{
  "sammeInntjeningsPeriode": true,
  "parametere": {
    "aktorId": "1234",
    "vedtakId": 5678,
    "beregningsdato": "2019-02-27",
    "inntektsId": "12345"
  }
}"""
}
