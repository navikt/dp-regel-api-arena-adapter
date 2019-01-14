package no.nav.dagpenger.regel.api.arena.adapter

import de.nielsfalk.ktor.swagger.description
import de.nielsfalk.ktor.swagger.examples
import de.nielsfalk.ktor.swagger.post
import de.nielsfalk.ktor.swagger.responds
import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing

@Group("Grunnlag")
@Location("/dagpengegrunnlag")
class PostGrunnlag

fun Routing.grunnlag(regelApiClient: RegelApiClient) {
    post<PostGrunnlag, DagpengegrunnlagBeregningsRequest>(
            "grunnlagberegning"
                    .description("Start grunnlagberegning")
                    .examples()
                    .responds()
    ) { _, request ->

        val taskUrl = regelApiClient.startGrunnlagBeregning(request)

        var taskResponse = regelApiClient.pollTask(taskUrl)
        while (taskResponse.task?.status == RegelApiClient.TaskStatus.PENDING) {
            taskResponse = regelApiClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val grunnlagBeregningResultat = regelApiClient.getGrunnlag(ressursLocation)

        call.respond(grunnlagBeregningResultat)
    }
}

data class DagpengegrunnlagBeregningsRequest(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val inntektsId: String,
    val bruktinntektsPeriode: InntektsPeriode,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean,
    val harArbeidsperiodeEosSiste12Maaneder: Boolean
)

data class InntektsPeriode(
    val foersteMaaned: String,
    val sisteMaaned: String
)

data class DagpengegrunnlagBeregningsResponse(
    val beregningsId: String,
    val utfall: Utfall,
    val opprettet: String,
    val utfort: String,
    val parametere: Parametere,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean,
    val harArbeidsperiodeEosSiste12Maaneder: Boolean
) {
    companion object {
        val exampleInntektBeregning = mapOf(
                "oppfyllerMinsteinntekt" to true,
                "status" to 1
        )
    }
}

data class Utfall(
    val oppfyllerKravtilMinsteArbeidsinntekt: Boolean,
    val periodeAntallUker: Int
)

data class Parametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val inntektsId: String,
    val bruktinntektsPeriode: InntektsPeriode
)