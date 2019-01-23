package no.nav.dagpenger.regel.api.arena.adapter

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import java.time.LocalDate
import java.time.LocalDateTime

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
    val beregningsdato: LocalDate,
    val bruktinntektsPeriode: InntektsPeriode? = null,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean
)

data class MinsteinntektBeregningsResponse(
    val beregningsId: String,
    val utfall: MinsteinntektUtfall,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: Parametere,
    val sisteInntektsPeriode: InntektsPeriode,
    val nestSisteInntektsPeriode: InntektsPeriode,
    val tredjeSisteInntektsPeriode: InntektsPeriode,
    val inntekt: Inntekt
)

data class MinsteinntektUtfall(
    val oppfyllerKravTilMinsteArbeidsinntekt: Boolean,
    val periodeAntallUker: Int
)

data class Inntekt(
    val inntektSistePeriode: Int,
    val inntektNestSistePeriode: Int,
    val inntektTredjeSistePeriode: Int,
    val andelInntektSistePeriode: Int? = null,
    val andelInntektNestSistePeriode: Int? = null,
    val andelInntektTredjeSistePeriode: Int? = null,
    val inneholderNaeringsinntekter: Boolean
)