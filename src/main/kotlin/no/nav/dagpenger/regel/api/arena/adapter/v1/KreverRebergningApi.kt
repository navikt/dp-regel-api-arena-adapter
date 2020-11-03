package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.internal.RegelApiReberegningHttpClient
import java.time.LocalDate

internal fun Route.KreverReberegningApi(reberegningHttpClient: RegelApiReberegningHttpClient) {
    route("/lovverk") {
        post("/krever-reberegning") {
            val parametere = call.receive<KreverReberegningParametere>()

            val resultat = reberegningHttpClient.kreverReberegning(parametere.subsumsjonIder, parametere.beregningsdato)
            call.respond(HttpStatusCode.OK, """{"reberegning": $resultat}""")
        }
    }
}

private data class KreverReberegningParametere(val beregningsdato: LocalDate, val subsumsjonIder: List<String>)
