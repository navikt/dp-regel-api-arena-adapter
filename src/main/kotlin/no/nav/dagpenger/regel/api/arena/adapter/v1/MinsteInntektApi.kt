package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.v1.minsteinntekt.SynchronousMinsteinntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektInnParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.mergeMinsteinntektOgPeriodeSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.periode.SynchronousPeriode

private val LOGGER = KotlinLogging.logger {}

fun Route.MinsteinntektApi(
    synchronousMinsteinntekt: SynchronousMinsteinntekt,
    synchronousPeriode: SynchronousPeriode
) {

    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektInnParametere>()

            val minsteinntektSubsumsjon = synchronousMinsteinntekt.getMinsteinntektSynchronously(parametere)

            val periodeSubsumsjon = synchronousPeriode.getPeriodeSynchronously(parametere)

            val minsteinntektOgPeriodeSubsumsjon =
                mergeMinsteinntektOgPeriodeSubsumsjon(minsteinntektSubsumsjon, periodeSubsumsjon)

            call.respond(HttpStatusCode.OK, minsteinntektOgPeriodeSubsumsjon)
        }
    }
}