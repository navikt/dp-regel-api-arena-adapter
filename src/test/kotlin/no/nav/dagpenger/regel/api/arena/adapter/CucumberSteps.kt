package no.nav.dagpenger.regel.api.arena.adapter

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cucumber.api.java8.No
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlin.test.assertFalse

class CucumberSteps : No {

    val moshi = Moshi.Builder()
        .add(YearMonthJsonAdapter())
        .add(LocalDateTimeJsonAdapter())
        .add(LocalDateJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    val minsteinntektBeregningsRequestAdapter = moshi.adapter(MinsteinntektBeregningsRequest::class.java)
    val minsteinntektBeregningsResponseAdapter = moshi.adapter(MinsteinntektBeregningsResponse::class.java)

    lateinit var request: MinsteinntektBeregningsRequest
    lateinit var minsteInntektResponse: MinsteinntektBeregningsResponse

    init {
        Gitt("at søker har inntekt under 1,5G siste år og under 3G siste 3 år") {
            request = Scenario1_1Request
        }

        Når("digidag skal vurdere minsteinntektkrav") {
            withTestApplication({ regelApiAdapter(RegelApiDummy()) }) {
                handleRequest(HttpMethod.Post, "/minsteinntekt") {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(minsteinntektBeregningsRequestAdapter.toJson(Scenario1_1Request))
                }.apply {
                    minsteInntektResponse = minsteinntektBeregningsResponseAdapter.fromJson(response.content) ?: throw RuntimeException()
                }
            }
        }

        Så("blir ikke kravet innfridd") {
            assertFalse(minsteInntektResponse.utfall.oppfyllerKravTilMinsteArbeidsinntekt)
        }

        fun testApp(callback: TestApplicationEngine.() -> Unit) {
            withTestApplication({ regelApiAdapter(RegelApiDummy()) }) { callback() }
        }
    }
}
