package no.nav.dagpenger.regel.api.arena.adapter

import cucumber.api.java8.No
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import io.mockk.mockkClass
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.internal.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.internal.sats.SynchronousSats
import no.nav.dagpenger.regel.api.internal.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.internal.periode.SynchronousPeriode
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DagpengergrunnlagApiV1Steps : No {


    private val dagpengegrunnlagInnParametereAdapter = moshiInstance.adapter<GrunnlagOgSatsParametere>(
        GrunnlagOgSatsParametere::class.java)
    private val dagpengegrunnlagBeregningAdapter = moshiInstance.adapter<GrunnlagOgSatsSubsumsjon>(
        GrunnlagOgSatsSubsumsjon::class.java)

    init {

        val synchronousMinsteinntekt: SynchronousMinsteinntekt = mockkClass(type = SynchronousMinsteinntekt::class)
        val synchronousPeriode: SynchronousPeriode = mockkClass(type = SynchronousPeriode::class)
        val synchronousGrunnlag: SynchronousGrunnlag = mockkClass(type = SynchronousGrunnlag::class)
        val synchronousSats: SynchronousSats = mockkClass(type = SynchronousSats::class)
        lateinit var dagpengegrunnlagInnParametere: GrunnlagOgSatsParametere
        lateinit var dagpengegrunnlagBeregning: GrunnlagOgSatsSubsumsjon
        Gitt("at søker med aktør id {string} med vedtak id {int} med beregningsdato {string} i beregning av grunnlag") { aktørId: String, vedtaktId: Int, beregningsDato: String ->
            dagpengegrunnlagInnParametere =
                GrunnlagOgSatsParametere(
                    aktorId = aktørId,
                    vedtakId = vedtaktId,
                    beregningsdato = LocalDate.parse(beregningsDato)
                )
        }

        Når("digidag skal beregne grunnlag") {
            withTestApplication({ regelApiAdapter(
                synchronousMinsteinntekt,
                synchronousPeriode,
                synchronousGrunnlag,
                synchronousSats,
                mockk()
            ) }) {
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
            assertTrue { dagpengegrunnlagBeregning.inntekt!!.size == 3 }
            val inntekt = dagpengegrunnlagBeregning.inntekt!!.map { it.periode to it }.toMap()
            assertEquals("2019-02", inntekt[1]?.inntektsPeriode?.sisteMaaned.toString())
        }
    }
}