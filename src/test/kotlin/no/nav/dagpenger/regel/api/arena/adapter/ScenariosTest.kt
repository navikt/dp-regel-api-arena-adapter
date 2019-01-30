package no.nav.dagpenger.regel.api.arena.adapter

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.regel.api.arena.adapter.alpha.DagpengegrunnlagBeregningsRequest
import no.nav.dagpenger.regel.api.arena.adapter.alpha.DagpengegrunnlagBeregningsResponse
import no.nav.dagpenger.regel.api.arena.adapter.alpha.MinsteinntektBeregningsRequest
import no.nav.dagpenger.regel.api.arena.adapter.alpha.MinsteinntektBeregningsResponse
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario1_1Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario1_2Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario1_3Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario2_1_AND_3_Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario2_2Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario3_1Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario3_4Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario4_1Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario4_2Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario5_1Request
import no.nav.dagpenger.regel.api.arena.adapter.alpha.Scenario5_2Request
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ScenariosTest {

    // Test and dummy responses as defined in https://confluence.adeo.no/display/TEAMARENA/ARENA-1892+-+04+Scenarier

    val minsteinntektBeregningsRequestAdapter = moshiInstance.adapter(MinsteinntektBeregningsRequest::class.java)
    val minsteinntektBeregningsResponseAdapter = moshiInstance.adapter(MinsteinntektBeregningsResponse::class.java)
    val grunnlagRequestAdapter = moshiInstance.adapter(DagpengegrunnlagBeregningsRequest::class.java)
    val grunnlagResponseAdapter = moshiInstance.adapter(DagpengegrunnlagBeregningsResponse::class.java)

    @Test
    fun `scenario 1-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario1_1Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "01D1XGEK0FNB179YPXB12TPDPT", beregning = response)
        }
    }

    @Test
    fun `scenario 1-2`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario1_2Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 52, inntektsId = "01D1XGEK15D0B6GDPC74C0ZASV", beregning = response)
        }
    }

    @Test
    fun `scenario 1-3`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario1_3Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 104, inntektsId = "01D1XGEK15D0B6GDPC74C0ZASV", beregning = response)
        }
    }

    @Test
    fun `scenario 2-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario2_1_AND_3_Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 52, inntektsId = "01D1XGEK2CA0X5BM7PZFYKS5WX", beregning = response)
        }
    }

    @Test
    fun `scenario 2-2`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario2_2Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "01D1XGEK2S8WWRZ0QE0Y7W414K", beregning = response)
        }
    }

    @Test
    fun `scenario 2-3`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario2_1_AND_3_Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 52, inntektsId = "01D1XGEK2CA0X5BM7PZFYKS5WX", beregning = response)
        }
    }

    @Test
    fun `scenario 3-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario3_1Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = false, periode = 0, inntektsId = "01D1XGEK398JX50S7P3V9ENH76", beregning = response)
        }
    }

    @Test
    fun `scenario 3-4`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(grunnlagRequestAdapter.toJson(Scenario3_4Request))
        }.apply {
            assert200OkResponse()
            val response = grunnlagResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertDagpengeGrunnlagResponse("01D1XGQB1BSQ4NGXCBMGQ5M2KF", response)
        }
    }

    @Test
    fun `scenario 4-1`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(grunnlagRequestAdapter.toJson(Scenario4_1Request))
        }.apply {
            assert200OkResponse()
            val response = grunnlagResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertDagpengeGrunnlagResponse("01D1XGQB3NNMHC54ADBGNF7HQG", response)
        }
    }

    @Test
    fun `scenario 4-2`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(grunnlagRequestAdapter.toJson(Scenario4_2Request))
        }.apply {
            assert200OkResponse()
            val response = grunnlagResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertDagpengeGrunnlagResponse("01D1XGQB49ZP6KCMX3FAP2XTNG", response)
        }
    }

    @Test
    fun `scenario 5-1`() = testApp {
        handleRequest(HttpMethod.Post, "/minsteinntekt") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario5_1Request))
        }.apply {
            assert200OkResponse()
            val response = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
            assertMinsteinntektResponse(oppfyllerKrav = true, periode = 104, inntektsId = "01D1XGEK151116GDPC74C0Z111", beregning = response)
        }
    }

    @Test
    fun `scenario 5-2`() = testApp {
        handleRequest(HttpMethod.Post, "/dagpengegrunnlag") {
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(grunnlagRequestAdapter.toJson(Scenario5_2Request))
        }.apply {
            assert400BadRequestResponse()
        }
    }

    fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ regelApiAdapter() }) { callback() }
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
    fun TestApplicationCall.assert400BadRequestResponse() {
        Assertions.assertTrue(requestHandled)
        Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
    }
}