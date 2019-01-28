package no.nav.dagpenger.regel.api.arena.adapter

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

fun Routing.grunnlag(regelApiClient: RegelApiClient) {
    post("/dagpengegrunnlag") {
        val request = call.receive<DagpengegrunnlagBeregningsRequest>()

        val taskUrl = regelApiClient.startGrunnlagBeregning(request)

        var taskResponse = regelApiClient.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiClient.pollTask(taskUrl)
        }

        val ressursLocation =
            taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val grunnlagBeregningResultat = regelApiClient.getGrunnlag(ressursLocation)

        call.respond(grunnlagBeregningResultat)
    }
}

data class DagpengegrunnlagBeregningsRequest(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val bruktinntektsPeriode: InntektsPeriode? = null,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean,
    val beregningsId: String? = null
)

data class InntektsPeriode(
    val foersteMaaned: YearMonth,
    val sisteMaaned: YearMonth
)

data class DagpengegrunnlagBeregningsResponse(
    val beregningsId: String,
    val utfall: UtfallGrunnlag,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: Parametere,
    val sisteInntektsPeriode: InntektsPeriode,
    val nestSisteInntektsPeriode: InntektsPeriode,
    val tredjeSisteInntektsPeriode: InntektsPeriode,
    val inntekt: Inntekt
)

data class UtfallGrunnlag(
    val dagpengegrunnlag: Int,
    val dagpengegrunnlagUavkortet: Int,
    val dagsatsUtenBarnetillegg: Int,
    val ukesatsMedBarnetillegg: Int,
    val beregningsregel: String,
    val benyttet90ProsentRegel: Boolean
)

data class Parametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String?,
    val bruktInntektsPeriode: InntektsPeriode?,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean
)