package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.kotest.assertions.json.shouldEqualJson
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
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat
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

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                inntektApiBeregningsdatoHttpClient = inntektApiBeregningsdatoHttpClient,
            )
        }) {
            handleRequest(HttpMethod.Post, inntjeningsperiodePath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
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
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                response.content?.shouldEqualJson(expectedJson)
            }
        }
    }

    @Test
    fun ` Should give 401 - Not authorized if token is missing `() {
        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
            )
        }) {
            handleRequest(HttpMethod.Post, inntjeningsperiodePath) {
                addHeader(HttpHeaders.ContentType, "application/json")
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
