package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.dagpenger.regel.api.internal.RegelApi
import java.time.LocalDate

internal fun Route.nyVurderingApi(regelApi: RegelApi) {
    route("/lovverk") {
        post("/vurdering/minsteinntekt") {
            withContext(Dispatchers.IO) {
                val parametere = call.receive<KreverReberegningParametere>()

                val resultat =
                    regelApi.kreverNyVurdering(parametere.subsumsjonIder, parametere.beregningsdato)
                call.respond(HttpStatusCode.OK, """{"reberegning": $resultat}""")
            }
        }
    }
}

private data class KreverReberegningParametere(
    val beregningsdato: LocalDate,
    val subsumsjonIder: List<String>,
)
