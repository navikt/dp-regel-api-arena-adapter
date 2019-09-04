package no.nav.dagpenger.regel.api.arena.adapter

import cucumber.api.java8.No
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import java.time.LocalDate
import kotlin.test.assertEquals

class DagpengergrunnlagApiV1Steps : No {

    private val dagpengegrunnlagInnParametereAdapter = moshiInstance.adapter<GrunnlagOgSatsParametere>(
        GrunnlagOgSatsParametere::class.java
    )
    private val dagpengegrunnlagBeregningAdapter = moshiInstance.adapter<GrunnlagOgSatsSubsumsjon>(
        GrunnlagOgSatsSubsumsjon::class.java
    )

    init {

        lateinit var dagpengegrunnlagInnParametere: GrunnlagOgSatsParametere
        lateinit var dagpengegrunnlagBeregning: GrunnlagOgSatsSubsumsjon
        lateinit var problem: Problem
        Gitt("at søker med aktør id {string} med vedtak id {int} med beregningsdato {string} i beregning av grunnlag") { aktørId: String, vedtaktId: Int, beregningsDato: String ->
            dagpengegrunnlagInnParametere =
                GrunnlagOgSatsParametere(
                    aktorId = aktørId,
                    vedtakId = vedtaktId,
                    beregningsdato = LocalDate.parse(beregningsDato)
                )
        }

        Når("digidag skal beregne grunnlag") {

            try {
                val response =
                    testApiClient.grunnlagOgSats(dagpengegrunnlagInnParametereAdapter.toJson(dagpengegrunnlagInnParametere))
                dagpengegrunnlagBeregning = response.parseJsonFrom(dagpengegrunnlagBeregningAdapter)
            } catch (ex: RegelApiArenaAdapterException) {
                problem = ex.problem
            }
        }

        Og("søker har avtjent verneplikt") {
            dagpengegrunnlagInnParametere = dagpengegrunnlagInnParametere.copy(harAvtjentVerneplikt = true)
        }

        Og("det er beregnet med et manuelt grunnlag på {int}") { manueltGrunnlag: Int ->
            dagpengegrunnlagInnParametere = dagpengegrunnlagInnParametere.copy(grunnlag = manueltGrunnlag)
        }

        Og("søker har {int} barn") { antallBarn: Int ->
            dagpengegrunnlagInnParametere = dagpengegrunnlagInnParametere.copy(antallBarn = antallBarn)
        }

        Så("er avkortet grunnlag satt til {int} og uavkortet til {int}") { avkortet: Int, uavkortet: Int ->
            assertEquals(avkortet, dagpengegrunnlagBeregning.resultat.grunnlag?.avkortet)
            assertEquals(uavkortet, dagpengegrunnlagBeregning.resultat.grunnlag?.uavkortet)
        }

        Og("er ukessats satt til {int}") { ukessats: Int ->
            assertEquals(ukessats, dagpengegrunnlagBeregning.resultat.sats.ukesats)
        }

        Så("dagsats satt til {int}") { dagsats: Int ->
            assertEquals(dagsats, dagpengegrunnlagBeregning.resultat.sats.dagsats)
        }

        Så("da er parameteret barn {int}") { antallBarn: Int ->
            assertEquals(antallBarn, dagpengegrunnlagBeregning.parametere.antallBarn)
        }

        Så("da er parameteret brukt nitti prosent regelen for barn satt") {

            // assertTrue(dagpengegrunnlagBeregning.resultat.benyttet90ProsentRegel)
        }

        Så("benyttet beregningsregel {string}") { beregningsregel: String ->
            assertEquals(beregningsregel, dagpengegrunnlagBeregning.resultat.beregningsRegel.toString())
        }

        Så("returneres en feil {string}") { feilmelding: String ->
            assertEquals(problem.title, feilmelding)
        }
    }
}