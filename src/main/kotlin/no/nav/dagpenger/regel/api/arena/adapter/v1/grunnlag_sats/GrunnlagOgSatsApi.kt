package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import de.huxhorn.sulky.ulid.ULID
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

    val ulidGenerator = ULID()
    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()

            val grunnlagSubsumsjon = synchronousGrunnlag.getGrunnlagSynchronously(parametere)

            parametere.grunnlag = grunnlagSubsumsjon.resultat.avkortet

            val satsSubsumsjon = synchronousSats.getSatsSynchronously(parametere)

            val grunnlagOgSatsSubsumsjon = mergeGrunnlagOgSatsSubsumsjon(grunnlagSubsumsjon, satsSubsumsjon)

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }
}