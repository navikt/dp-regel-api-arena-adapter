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

@Group("Minsteinntekt")
@Location("/minsteinntekt")
class PostMinsteinntekt

fun Routing.minsteinntekt(regelApiClient: RegelApiClient) {
    post<PostMinsteinntekt, MinsteinntektBeregningsRequest>(
        "minsteinntektsberegning"
            .description("Start minsteinntektberegning")
            .examples()
//            .responds(
//                ok<MinsteinntektBeregningsResponse>(
//                    example(
//                        "",
//                        MinsteinntektBeregningsResponse(
//                            "456",
//                            Utfall(true, 104),
//                            "2018-12-26T14:42:09Z",
//                            "2018-12-26T14:42:09Z",
//                            Parametere(
//                                "01019955667",
//                                123,
//                                "2019-01-11",
//                                "lasdfQ",
//                                InntektsPeriode("2019-01", "2018-01"),
//                                false,
//                                false
//                            ),
//                            InntektsPeriode("2018-01", "2018-12"),
//                            InntektsPeriode("2017-01", "2017-12"),
//                            InntektsPeriode("2016-01", "2016-12"),
//                            Inntekt(
//                                BigDecimal(50000),
//                                BigDecimal(0),
//                                BigDecimal(0),
//                                inneholderNaeringsinntekter = false
//                            )
//                        )
//                    )
//                )
//            )
    ) { _, request ->

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
    val bruktinntektsPeriode: InntektsPeriode?,
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
) {
    companion object {
        val exampleInntektBeregning = mapOf(
            "oppfyllerMinsteinntekt" to true,
            "status" to 1
        )
    }
}

data class Inntekt(
    val inntektSistePeriode: BigDecimal,
    val inntektNestSistePeriode: BigDecimal,
    val inntektTredjeSistePeriode: BigDecimal,
    val andelInntektSistePeriode: BigDecimal? = null,
    val andelInntektNestSistePeriode: BigDecimal? = null,
    val andelInntektTredjeSistePeriode: BigDecimal? = null,
    val inneholderNaeringsinntekter: Boolean
)