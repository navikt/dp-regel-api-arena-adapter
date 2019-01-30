package no.nav.dagpenger.regel.api.arena.adapter.alpha

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import no.nav.dagpenger.regel.api.arena.adapter.RegelApiArenaAdapterException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Deprecated("use DagpengegrunnlagApi", replaceWith = ReplaceWith("no.nav.dagpenger.regel.api.arena.adapter.DagpengegrunnlagApi.kt"))
fun Routing.grunnlag(regelApiClientAlpha: RegelApiClientAlpha = RegelApiDummyForAlphaAlpha()) {
    post("/dagpengegrunnlag") {
        val request = call.receive<DagpengegrunnlagBeregningsRequest>()

        val taskUrl = regelApiClientAlpha.startGrunnlagBeregning(request)

        var taskResponse = regelApiClientAlpha.pollTask(taskUrl)
        while (taskResponse.task?.status == TaskStatus.PENDING) {
            taskResponse = regelApiClientAlpha.pollTask(taskUrl)
        }

        val ressursLocation =
            taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val grunnlagBeregningResultat = regelApiClientAlpha.getGrunnlag(ressursLocation)

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