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
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntjeningsperiodeResultat
import no.nav.dagpenger.regel.api.internal.InntektApiInntjeningsperiodeHttpClient

private val sikkerlogg = KotlinLogging.logger("tjenestekall.grunnlagOgSatsApi")

internal fun Route.InntjeningsperiodeApi(inntektApiberegningsdatoHttpClient: InntektApiInntjeningsperiodeHttpClient) {
    route("/inntjeningsperiode") {
        post {
            withContext(Dispatchers.IO) {
                val parametere = call.receive<InntjeningsperiodeParametre>()

                val parametereInternal = no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre(
                    parametere.aktorId,
                    parametere.vedtakId,
                    parametere.beregningsdato,
                    parametere.inntektsId,
                )
                val resultatInternal = inntektApiberegningsdatoHttpClient.getInntjeningsperiode(parametereInternal)

                val resultat = InntjeningsperiodeResultat(
                    resultatInternal.sammeInntjeningsPeriode,
                    InntjeningsperiodeParametre(
                        resultatInternal.parametere.aktorId,
                        resultatInternal.parametere.vedtakId,
                        resultatInternal.parametere.beregningsdato,
                        resultatInternal.parametere.inntektsId,
                    ),
                )

                call.respond(HttpStatusCode.OK, resultat)
            }
        }
    }
}
