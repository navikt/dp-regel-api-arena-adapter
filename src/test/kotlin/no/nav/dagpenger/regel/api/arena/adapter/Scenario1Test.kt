package no.nav.dagpenger.regel.api.arena.adapter

import com.google.gson.Gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Scenario1Test {

    val json1_1 = """
{
  "aktorId": "1000033752789",
  "beregningsdato": "2019-01-10",
  "harAvtjentVerneplikt": false,
  "oppfyllerKravTilFangstOgFisk": false,
  "vedtakId": 31018297
}
""".trimIndent()

    val json1_2 = """
{
  "aktorId": "1000033752789",
  "beregningsdato": "2019-02-06",
  "harAvtjentVerneplikt": false,
  "oppfyllerKravTilFangstOgFisk": false,
  "vedtakId": 31018297
}
""".trimIndent()

    val json1_3 = """
{
  "aktorId": "1000033752789",
  "beregningsdato": "2019-02-06",
  "harAvtjentVerneplikt": false,
  "oppfyllerKravTilFangstOgFisk": true,
  "vedtakId": 31018297
}
""".trimIndent()
    @Test
    fun `scenario 1-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(json1_1)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())

            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)

            assertEquals(false, response.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
            assertEquals("A", response.parametere.inntektsId)
            assertEquals("M1", response.beregningsId)
        }
    }

    @Test
    fun `scenario 1-2`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(json1_2)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())

            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)

            assertEquals(true, response.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
            assertEquals(52, response.utfall.periodeAntallUker)
            assertEquals("B", response.parametere.inntektsId)
            assertEquals("M2", response.beregningsId)
        }
    }

    @Test
    fun `scenario 1-3`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(json1_3)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())

            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)

            assertEquals(true, response.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
            assertEquals(104, response.utfall.periodeAntallUker)
            assertEquals("B", response.parametere.inntektsId)
            assertEquals("M3", response.beregningsId)
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ regelApiAdapter(RegelApiDummy()) }) { callback() }
    }
}
