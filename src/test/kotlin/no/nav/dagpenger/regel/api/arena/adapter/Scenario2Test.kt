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

class Scenario2Test {

    val json2_1 = """
{
  "aktorId": "1000003221752",
  "beregningsdato": "2019-01-11",
  "harAvtjentVerneplikt": false,
  "oppfyllerKravTilFangstOgFisk": false,
  "vedtakId": 31018347
}
""".trimIndent()

    val json2_2 = """
{
  "aktorId": "1000003221752",
  "beregningsdato": "2019-02-07",
  "harAvtjentVerneplikt": false,
  "oppfyllerKravTilFangstOgFisk": false,
  "vedtakId": 31018347
}
""".trimIndent()

    val json2_3 = """
{
  "aktorId": "1000003221752",
  "beregningsdato": "2019-01-11",
  "harAvtjentVerneplikt": false,
  "oppfyllerKravTilFangstOgFisk": false,
  "vedtakId": 31018347
}
""".trimIndent()

    @Test
    fun `scenario 2-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(json2_1)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())

            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)

            assertEquals(true, response.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
            assertEquals(52, response.utfall.periodeAntallUker)
            assertEquals("C", response.parametere.inntektsId)
            assertEquals("M4", response.beregningsId)
        }
    }

    @Test
    fun `scenario 2-2`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(json2_2)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())

            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)

            assertEquals(false, response.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
            assertEquals(0, response.utfall.periodeAntallUker)
            assertEquals("D", response.parametere.inntektsId)
            assertEquals("M5", response.beregningsId)
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ regelApiAdapter(RegelApiDummy()) }) { callback() }
    }
}
