package no.nav.dagpenger.regel.api.arena.adapter

import com.squareup.moshi.JsonAdapter
import cucumber.api.java8.No
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektBeregning
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektInnParametere
import java.time.LocalDate
import kotlin.test.assertEquals

class MinsteinntektApiV1Steps : No {

    val v1MinsteinntektPath = "/v1/minsteinntekt"

    val minsteinntektInnParametereAdapter: JsonAdapter<MinsteinntektInnParametere> =
        moshiInstance.adapter<MinsteinntektInnParametere>(MinsteinntektInnParametere::class.java)
    val minsteinntektBeregningAdapter: JsonAdapter<MinsteinntektBeregning> =
        moshiInstance.adapter<MinsteinntektBeregning>(MinsteinntektBeregning::class.java)

    init {

        lateinit var minsteinntektInnParametere: MinsteinntektInnParametere
        lateinit var minsteinntektBeregning: MinsteinntektBeregning
        Gitt("at søker med aktør id {string} med vedtak id {int} med beregningsdato {string}") { aktørId: String, vedtakId: Int, beregningsDato: String ->
            minsteinntektInnParametere = MinsteinntektInnParametere(
                aktorId = aktørId,
                vedtakId = vedtakId,
                beregningsdato = LocalDate.parse(beregningsDato)
            )
        }

        Når("digidag skal vurdere minsteinntektkrav") {
            withTestApplication({ regelApiAdapter() }) {
                handleRequest(HttpMethod.Post, v1MinsteinntektPath) {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(minsteinntektInnParametereAdapter.toJson(minsteinntektInnParametere))
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    minsteinntektBeregning = response.content.parseJsonFrom(minsteinntektBeregningAdapter)
                }
            }
        }

        Så("kravet til minsteinntekt er {string}") { utfall: String ->
            assertEquals(utfall.equals("oppfylt"), minsteinntektBeregning.resultat.oppfyllerKravTilMinsteArbeidsinntekt)
        }

        Og("har krav på {int} uker") { periodeAntallUker: Int ->
            if (periodeAntallUker > 0) {
                assertEquals(periodeAntallUker, minsteinntektBeregning.resultat.periodeAntallUker)
            }
        }
    }

    fun <T> String?.parseJsonFrom(adapter: JsonAdapter<T>): T {
        return this?.let { adapter.fromJson(it) } ?: throw AssertionError("No content from server")
    }
}