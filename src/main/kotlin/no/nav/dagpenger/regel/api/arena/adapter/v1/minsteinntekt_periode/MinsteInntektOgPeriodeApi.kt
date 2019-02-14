package no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt_periode.periode.SynchronousPeriode

private val LOGGER = KotlinLogging.logger {}

fun Route.MinsteinntektOgPeriodeApi(
    synchronousMinsteinntekt: SynchronousMinsteinntekt,
    synchronousPeriode: SynchronousPeriode
) {

    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektOgPeriodeParametere>()

            val minsteinntektSubsumsjon = synchronousMinsteinntekt.getMinsteinntektSynchronously(parametere)

            val periodeSubsumsjon = synchronousPeriode.getPeriodeSynchronously(parametere)

            val minsteinntektOgPeriodeSubsumsjon =
                mergeMinsteinntektOgPeriodeSubsumsjon(minsteinntektSubsumsjon, periodeSubsumsjon)

            call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
        }
    }
}