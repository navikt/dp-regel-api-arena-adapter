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

@Group("Minsteinntekt")
@Location("/minsteinntekt")
class PostMinsteinntekt

fun Routing.minsteinntekt(regelApiClient: RegelApiClient) {
    post<PostMinsteinntekt, MinsteinntektBeregningsRequest>(
            "minsteinntektsberegning"
                    .description("Start minsteinntektberegning")
                    .examples()
                    .responds(ok<MinsteinntektBeregningsResponse>(
                            example("",
                                    MinsteinntektBeregningsResponse(
                                            "456",
                                            Utfall(true, 104),
                                            "2018-12-26T14:42:09Z",
                                            "2018-12-26T14:42:09Z",
                                            Parametere(
                                                    "01019955667",
                                                    123,
                                                    "2019-01-11",
                                                    "lasdfQ",
                                                    InntektsPeriode("2019-01", "2018-01")
                                            ),
                                            false,
                                            false,
                                            false
                                    ))
                    ))
    ) { _, request ->

        val taskUrl = regelApiClient.startMinsteinntktBeregning(request)

        var taskResponse = regelApiClient.pollTask(taskUrl)
        while (taskResponse.task?.status == RegelApiClient.TaskStatus.PENDING) {
            taskResponse = regelApiClient.pollTask(taskUrl)
        }

        val ressursLocation = taskResponse.location ?: throw RegelApiArenaAdapterException("Did not get location with task")

        val minsteinntektBeregningResultat = regelApiClient.getMinsteinntekt(ressursLocation)

        call.respond(minsteinntektBeregningResultat)
    }
}

data class MinsteinntektBeregningsRequest(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: String,
    val inntektsId: String,
    val bruktinntektsPeriode: InntektsPeriode,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean,
    val harArbeidsperiodeEosSiste12Maaneder: Boolean
)

data class MinsteinntektBeregningsResponse(
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