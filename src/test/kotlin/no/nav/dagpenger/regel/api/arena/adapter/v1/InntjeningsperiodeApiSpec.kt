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
import no.nav.dagpenger.regel.api.internal.inntjeningsperiode.InntektApiBeregningsdatoHttpClient
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class InntjeningsperiodeApiSpec {
    @Test
    fun `Inntjeningsperiode API specification test - Should match json field names and formats`() {

        val inntektApiBeregningsdatoHttpClient: InntektApiBeregningsdatoHttpClient = mockk()

        every { runBlocking {
            inntektApiBeregningsdatoHttpClient.getBeregningsdato(any())
        } } returns LocalDate.of(2019, 2, 27)

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