package no.nav.dagpenger.regel.api.arena.adapter

import cucumber.api.java8.No
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.GrunnlagOgSatsParametere
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DagpengergrunnlagApiV1Steps : No {

    val dagpengegrunnlagInnParametereAdapter = moshiInstance.adapter<GrunnlagOgSatsParametere>(GrunnlagOgSatsParametere::class.java)
    val dagpengegrunnlagBeregningAdapter = moshiInstance.adapter<GrunnlagOgSatsSubsumsjon>(GrunnlagOgSatsSubsumsjon::class.java)

    init {

        lateinit var dagpengegrunnlagInnParametere: GrunnlagOgSatsParametere
        lateinit var dagpengegrunnlagBeregning: GrunnlagOgSatsSubsumsjon
        Gitt("at søker med aktør id {string} med vedtak id {int} med beregningsdato {string} i beregning av grunnlag") { aktørId: String, vedtaktId: Int, beregningsDato: String ->
            dagpengegrunnlagInnParametere = GrunnlagOgSatsParametere(
                aktorId = aktørId,
                vedtakId = vedtaktId,
                beregningsdato = LocalDate.parse(beregningsDato)
            )
        }

        Når("digidag skal beregne grunnlag") {
            withTestApplication({ regelApiAdapter() }) {
                handleRequest(HttpMethod.Post, "/v1/dagpengegrunnlag") {
                    addHeader(HttpHeaders.ContentType, "application/json")
                    setBody(dagpengegrunnlagInnParametereAdapter.toJson(dagpengegrunnlagInnParametere))
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    dagpengegrunnlagBeregning = response.content.parseJsonFrom(dagpengegrunnlagBeregningAdapter)
                }
            }
        }

        Så("er vedtak id {int}") { vedtakId: Int ->
            assertEquals(vedtakId, dagpengegrunnlagBeregning.parametere.vedtakId)
            assertTrue { dagpengegrunnlagBeregning.inntekt.size == 3 }
            val inntekt = dagpengegrunnlagBeregning.inntekt.map { it.periode to it }.toMap()
            assertEquals("2019-02", inntekt[1]?.inntektsPeriode?.sisteMaaned.toString())
        }
    }
}