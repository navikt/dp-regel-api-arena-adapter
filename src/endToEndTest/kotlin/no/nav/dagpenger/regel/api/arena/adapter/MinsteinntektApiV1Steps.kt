package no.nav.dagpenger.regel.api.arena.adapter

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonAdapter
import cucumber.api.java8.No


import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere

import java.time.LocalDate
import kotlin.test.assertEquals

class MinsteinntektApiV1Steps : No {

    val v1MinsteinntektPath = "/v1/minsteinntekt"

    val minsteinntektInnParametereAdapter: JsonAdapter<MinsteinntektOgPeriodeParametere> =
        moshiInstance.adapter<MinsteinntektOgPeriodeParametere>(
            MinsteinntektOgPeriodeParametere::class.java
        )
    val minsteinntektBeregningAdapter: JsonAdapter<MinsteinntektOgPeriodeSubsumsjon> =
        moshiInstance.adapter<MinsteinntektOgPeriodeSubsumsjon>(
            MinsteinntektOgPeriodeSubsumsjon::class.java
        )

    init {

        lateinit var minsteinntektInnParametere: MinsteinntektOgPeriodeParametere
        lateinit var minsteinntektBeregning: MinsteinntektOgPeriodeSubsumsjon


        Gitt("at søker med aktør id {string} med vedtak id {int} med beregningsdato {string}") { aktørId: String, vedtakId: Int, beregningsDato: String ->
            minsteinntektInnParametere =
                MinsteinntektOgPeriodeParametere(
                    aktorId = aktørId,
                    vedtakId = vedtakId,
                    beregningsdato = LocalDate.parse(beregningsDato)
                )
        }

        Når("digidag skal vurdere minsteinntektkrav") {

           val response =  apiRequest(minsteinntektInnParametere)
            minsteinntektBeregning  = response.parseJsonFrom(minsteinntektBeregningAdapter)




        }

        Så("kravet til minsteinntekt er {string}") { utfall: String ->
            assertEquals(utfall == "oppfylt", minsteinntektBeregning.resultat.oppfyllerKravTilMinsteArbeidsinntekt)
        }

        Og("har krav på {int} uker") { periodeAntallUker: Int ->
            if (periodeAntallUker > 0) {
                assertEquals(periodeAntallUker, minsteinntektBeregning.resultat.periodeAntallUker)
            }
        }

        Gitt("at søker har ingen inntekt siste {int} måneder") { måneder: Int ->
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

    private fun apiRequest(minsteinntektInnParametere: MinsteinntektOgPeriodeParametere): String {
        val (_, response, result) = with(
            "https://dp-regel-api-arena-adapter.nais.preprod.local/v1/minsteinntekt".httpPost()
                .header("Content-Type" to "application/json")
                .body(minsteinntektInnParametereAdapter.toJson(minsteinntektInnParametere))
        )
        {
            responseString()
        }

        return when (result) {
            is Result.Failure -> throw AssertionError(
                "Failed post to adapter. Response message ${response.responseMessage}. Error message: ${result.error.message}"
            )
            is Result.Success -> result.get()
        }
    }
}
