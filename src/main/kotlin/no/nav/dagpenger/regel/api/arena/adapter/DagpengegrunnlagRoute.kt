package no.nav.dagpenger.regel.api.arena.adapter

import de.nielsfalk.ktor.swagger.description
import de.nielsfalk.ktor.swagger.example
import de.nielsfalk.ktor.swagger.examples
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.post
import de.nielsfalk.ktor.swagger.responds
import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import java.math.BigDecimal

@Group("Grunnlag")
@Location("/dagpengegrunnlag")
class PostGrunnlag

fun Routing.grunnlag(regelApiClient: RegelApiClient) {
    post<PostGrunnlag, DagpengegrunnlagBeregningsRequest>(
        "grunnlagberegning"
            .description("Start grunnlagberegning")
            .examples()
    ) { _, request ->

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
    val beregningsdato: String,
    val bruktinntektsPeriode: InntektsPeriode? = null,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean
)

data class InntektsPeriode(
    val foersteMaaned: String,
    val sisteMaaned: String
)

data class DagpengegrunnlagBeregningsResponse(
    val beregningsId: String,
    val utfall: UtfallGrunnlag,
    val opprettet: String,
    val utfort: String,
    val parametere: Parametere,
    val sisteInntektsPeriode: InntektsPeriode,
    val nestSisteInntektsPeriode: InntektsPeriode,
    val tredjeSisteInntektsPeriode: InntektsPeriode,
    val inntekt: Inntekt
)

data class UtfallGrunnlag(
    val dagpengegrunnlag: BigDecimal,
    val dagpengegrunnlagUavkortet: BigDecimal,
    val dagsatsUtenBarnetillegg: BigDecimal,
    val ukesatsMedBarnetillegg: BigDecimal,
    val beregningsregel: String,
    val benyttet90ProsentRegel: Boolean
)

data class Parametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val inntektsId: String,
    val bruktInntektsPeriode: InntektsPeriode?,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean
)