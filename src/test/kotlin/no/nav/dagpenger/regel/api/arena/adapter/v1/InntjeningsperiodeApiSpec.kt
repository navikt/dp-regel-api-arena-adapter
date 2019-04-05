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
import no.nav.dagpenger.regel.api.internal.inntjeningsperiode.InntektApiInntjeningsperiodeHttpClient
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InntjeningsperiodeApiSpec {
    @Test
    fun `Inntjeningsperiode API specification test - Should match json field names and formats`() {

        val inntektApiBeregningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient = mockk()

        every { runBlocking {
            inntektApiBeregningsdatoHttpClient.getInntjeningsperiode(any())
        } } returns InntjeningsperiodeResultat(
            true,
            InntjeningsperiodeParametre(
                "1234",
                5678,
                "2019-02-27",
                "12345"
            )
        )

        withTestApplication({
            regelApiAdapter(
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                inntektApiBeregningsdatoHttpClient
            )
        }) {
            handleRequest(HttpMethod.Post, "/v1/inntjeningsperiode") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(
                    """
                    {
                      "aktorId": "1234",
                      "vedtakId": 5678,
                      "beregningsdato": "2019-02-27",
                      "inntektsId": "12345"
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
        """{"sammeInntjeningsPeriode":true,"parametere":{"aktorId":"1234","vedtakId":5678,"beregningsdato":"2019-02-27","inntektsId":"12345"}}"""
}