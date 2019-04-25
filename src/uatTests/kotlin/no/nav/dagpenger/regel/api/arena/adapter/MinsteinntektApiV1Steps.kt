package no.nav.dagpenger.regel.api.arena.adapter

import com.squareup.moshi.JsonAdapter
import cucumber.api.java8.No
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeSubsumsjon
import org.apache.logging.log4j.LogManager
import java.time.LocalDate
import java.time.YearMonth

import kotlin.test.assertEquals

private val logger = LogManager.getLogger()

class MinsteinntektApiV1Steps : No {

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

        Når("digidag skal vurdere minsteinntektkrav og periode") {
            val response =
                testApiClient.minsteinntektOgPeriode(minsteinntektInnParametereAdapter.toJson(minsteinntektInnParametere))
            minsteinntektBeregning = response.parseJsonFrom(minsteinntektBeregningAdapter)
        }

        Så("kravet til minsteinntekt er {string}") { utfall: String ->
            assertEquals(utfall == "oppfylt", minsteinntektBeregning.resultat.oppfyllerKravTilMinsteArbeidsinntekt)
        }

        Og("har krav på {int} uker") { periodeAntallUker: Int ->
            if (periodeAntallUker > 0) { // todo Should not have perioderesultat if minsteinntektOgPeriode not validated
                assertEquals(periodeAntallUker, minsteinntektBeregning.resultat.periodeAntallUker)
            }
        }

        Gitt("har avtjent verneplikt") {
            // Write code here that turns the phrase above into concrete actions
            minsteinntektInnParametere = minsteinntektInnParametere.copy(harAvtjentVerneplikt = true)
        }

        Gitt("hvor brukt inntekt er fra førstemåned {string} og sistemåned {string}") { førstemåned: String, sistemåned: String ->
            // Write code here that turns the phrase above into concrete actions
            minsteinntektInnParametere = minsteinntektInnParametere.copy(
                bruktInntektsPeriode = InntektsPeriode(
                    foersteMaaned = YearMonth.parse(førstemåned),
                    sisteMaaned = YearMonth.parse(sistemåned)
                )
            )
        }

        Gitt("at søker skal ha medberegnet inntekt fra fangst og fisk") {
            // Write code here that turns the phrase above into concrete actions
            minsteinntektInnParametere = minsteinntektInnParametere.copy(oppfyllerKravTilFangstOgFisk = true)
        }

        Så("inntektene inneholder fangs og fisk") {
            // Write code here that turns the phrase above into concrete actions
            minsteinntektBeregning.inntekt.first().inneholderNaeringsinntekter
        }
    }
}
