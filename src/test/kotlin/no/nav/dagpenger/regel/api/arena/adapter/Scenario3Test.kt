package no.nav.dagpenger.regel.api.arena.adapter

import com.google.gson.Gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.junit.jupiter.api.Assertions

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class Scenario3Test {

    @Test
    fun `scenario 3_1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario3_1Request))
        }.apply {
            Assertions.assertTrue(requestHandled)
            Assertions.assertEquals(HttpStatusCode.OK, response.status())

            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)

            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "J", beregning = response)
        }
    }
}

fun assertMinsteinntektResponse(
    oppfyllerKrav: Boolean,
    periode: Int,
    inntektsId: String,
    beregning: MinsteinntektBeregningsResponse
) {
    Assertions.assertEquals(oppfyllerKrav, beregning.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
    Assertions.assertEquals(periode, beregning.utfall.periodeAntallUker)
    Assertions.assertEquals(inntektsId, beregning.parametere.inntektsId)
    assertNotNull("Beregnings id er satt og unik", beregning.beregningsId)
}