package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SynchronousSats

fun Route.GrunnlagOgSatsApi(
    synchronousGrunnlag: SynchronousGrunnlag,
    synchronousSats: SynchronousSats
) {

    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()
            var grunnlagOgSatsSubsumsjon: GrunnlagOgSatsSubsumsjon

            if (parametere.grunnlag == null) {
                val grunnlagSubsumsjon = synchronousGrunnlag.getGrunnlagSynchronously(parametere)

                parametere.grunnlag = grunnlagSubsumsjon.resultat.avkortet

                val satsSubsumsjon = synchronousSats.getSatsSynchronously(parametere)

                grunnlagOgSatsSubsumsjon = mergeGrunnlagOgSatsSubsumsjon(grunnlagSubsumsjon, satsSubsumsjon)
            } else {
                val satsSubsumsjon = synchronousSats.getSatsSynchronously(parametere)

                grunnlagOgSatsSubsumsjon = mapGrunnlagOgSatsSubsumsjon(satsSubsumsjon)
            }

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }
}