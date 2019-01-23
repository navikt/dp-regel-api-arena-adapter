package no.nav.dagpenger.regel.api.arena.adapter

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import java.math.BigDecimal

fun Routing.minsteinntekt(regelApiClient: RegelApiClient) {

    post("/minsteinntekt") {
        val request = call.receive<MinsteinntektBeregningsRequest>()

        val taskUrl = regelApiClient.startMinsteinntktBeregning(request)

        var taskResponse = regelApiClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiClient.pollTask(taskUrl)
        }

        val ressursLocation =
            taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektBeregningResultat = regelApiClient.getMinsteinntekt(ressursLocation)

        call.respond(minsteinntektBeregningResultat)
    }
}

data class MinsteinntektBeregningsRequest(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val bruktinntektsPeriode: InntektsPeriode? = null,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean
)

data class MinsteinntektBeregningsResponse(
    val beregningsId: String,
    val utfall: Utfall,
    val opprettet: String,
    val utfort: String,
    val parametere: Parametere,
    val sisteInntektsPeriode: InntektsPeriode,
    val nestSisteInntektsPeriode: InntektsPeriode,
    val tredjeSisteInntektsPeriode: InntektsPeriode,
    val inntekt: Inntekt
)

data class Utfall(
    val oppfyllerKravTilMinsteArbeidsinntekt: Boolean,
    val periodeAntallUker: Int
)

data class Inntekt(
    val inntektSistePeriode: BigDecimal,
    val inntektNestSistePeriode: BigDecimal,
    val inntektTredjeSistePeriode: BigDecimal,
    val andelInntektSistePeriode: BigDecimal? = null,
    val andelInntektNestSistePeriode: BigDecimal? = null,
    val andelInntektTredjeSistePeriode: BigDecimal? = null,
    val inneholderNaeringsinntekter: Boolean
)