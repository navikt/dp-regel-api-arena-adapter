package no.nav.dagpenger.regel.api.arena.adapter

import com.google.gson.Gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ScenariosTest {

    // Test and dummy responses as defined in https://confluence.adeo.no/display/TEAMARENA/ARENA-1892+-+04+Scenarier

    @Test
    fun `scenario 1-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario1_1Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "01D1XGEK0FNB179YPXB12TPDPT", beregning = response)
        }
    }

    @Test
    fun `scenario 1-2`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario1_2Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 52, inntektsId = "01D1XGEK15D0B6GDPC74C0ZASV", beregning = response)
        }
    }

    @Test
    fun `scenario 1-3`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario1_3Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 104, inntektsId = "01D1XGEK1T355GP4FWQ51XP153", beregning = response)
        }
    }

    @Test
    fun `scenario 2-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario2_1_AND_3_Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 52, inntektsId = "01D1XGEK2CA0X5BM7PZFYKS5WX", beregning = response)
        }
    }

    @Test
    fun `scenario 2-2`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario2_2Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "01D1XGEK2S8WWRZ0QE0Y7W414K", beregning = response)
        }
    }

    @Test
    fun `scenario 2-3`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario2_1_AND_3_Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 52, inntektsId = "01D1XGEK2CA0X5BM7PZFYKS5WX", beregning = response)
        }
    }

    @Test
    fun `scenario 3_1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(Scenario3_1Request))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, MinsteinntektBeregningsResponse::class.java)
            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "01D1XGEK398JX50S7P3V9ENH76", beregning = response)
        }
    }

    @Test
    fun `grunnlagscenario 1`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(GrunnlagScenario1))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, DagpengegrunnlagBeregningsResponse::class.java)
            assertDagpengeGrunnlagResponse("01D1XGQB1BSQ4NGXCBMGQ5M2KF", response)
        }
    }

    @Test
    fun `grunnlagscenario 2`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(GrunnlagScenario2))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, DagpengegrunnlagBeregningsResponse::class.java)
            assertDagpengeGrunnlagResponse("01D1XGQB3NNMHC54ADBGNF7HQG", response)
        }
    }

    @Test
    fun `grunnlagscenario 3`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(Gson().toJson(GrunnlagScenario3))
        }.apply {
            assert200OkResponse()
            val response = Gson().fromJson(response.content, DagpengegrunnlagBeregningsResponse::class.java)
            assertDagpengeGrunnlagResponse("01D1XGQB49ZP6KCMX3FAP2XTNG", response)
        }
    }

    fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ regelApiAdapter(RegelApiDummy()) }) { callback() }
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

    fun assertDagpengeGrunnlagResponse(
        inntektsId: String,
        grunnlag: DagpengegrunnlagBeregningsResponse
    ) {
        Assertions.assertEquals(inntektsId, grunnlag.parametere.inntektsId)
        assertNotNull("Beregnings id er satt og unik", grunnlag.beregningsId)
    }

    fun TestApplicationCall.assert200OkResponse() {
        Assertions.assertTrue(requestHandled)
        Assertions.assertEquals(HttpStatusCode.OK, response.status())
    }
}