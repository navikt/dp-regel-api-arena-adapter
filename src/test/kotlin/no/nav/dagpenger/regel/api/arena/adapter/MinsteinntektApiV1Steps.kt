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

        Gitt("at søker har ingen inntekt siste {int} måneder") { int1: Int ->
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Når("digidag skal vurdere minsteinntektkrav og periode") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("har avtjent verneplikt") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Når("digidag skal vrudere søknaden") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("er kravet til minsteinntekt {string}") { string: String ->
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("perioden er {int} uker") { int1: Int ->
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("grunnlaget er XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("satsen er XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("at grunnlag er satt manuelt til XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Når("digidag skal sette sats") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("er ukessats satt til XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("dagsats satt til XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("at søker har fått minsteinntekt oppfylt") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("søker skal få barnetilleg") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("er det lagt til {int} * {int} * antall barn på ukessats") { int1: Int, int2: Int ->
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("at søker har tjent {double}G siste {int} måneder") { double1: Double, int1: Int ->
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Når("digidag skal vurdere søknaden") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("grunnlaget er satt til XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("dagsatsen er satt til XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("ukessatsen er satt til XXX") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("at søker skal ha medberegnet inntekt fra fangst og fisk") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("tas denne inntekten med") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Gitt("at noe av inntekten er benyttet fra før") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }

        Så("skal denne trekkes fra") {
            // Write code here that turns the phrase above into concrete actions
            throw cucumber.api.PendingException()
        }
    }

    fun <T> String?.parseJsonFrom(adapter: JsonAdapter<T>): T {
        return this?.let { adapter.fromJson(it) } ?: throw AssertionError("No content from server")
    }
}
