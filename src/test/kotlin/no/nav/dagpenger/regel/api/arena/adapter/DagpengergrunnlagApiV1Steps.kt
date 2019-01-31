package no.nav.dagpenger.regel.api.arena.adapter

import cucumber.api.java8.No
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.DagpengegrunnlagBeregning
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.DagpengegrunnlagInnParametere
import java.time.LocalDate
import kotlin.test.assertEquals

class DagpengergrunnlagApiV1Steps : No {

    val dagpengegrunnlagInnParametereAdapter = moshiInstance.adapter<DagpengegrunnlagInnParametere>(DagpengegrunnlagInnParametere::class.java)
    val dagpengegrunnlagBeregningAdapter = moshiInstance.adapter<DagpengegrunnlagBeregning>(DagpengegrunnlagBeregning::class.java)

    init {

        lateinit var dagpengegrunnlagInnParametere: DagpengegrunnlagInnParametere
        lateinit var dagpengegrunnlagBeregning: DagpengegrunnlagBeregning
        Gitt("at søker med aktør id {string} med vedtak id {int} med beregningsdato {string} i beregning av grunnlag") { aktørId: String, vedtaktId: Int, beregningsDato: String ->
            dagpengegrunnlagInnParametere = DagpengegrunnlagInnParametere(
                aktorId = aktørId,
                vedtakId = vedtaktId,
                beregningsDato = LocalDate.parse(beregningsDato)
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
        }
    }
}