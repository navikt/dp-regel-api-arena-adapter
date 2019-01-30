package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektBeregning
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektInnParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt.MinsteinntektResultatParametere
import java.time.LocalDate
import java.time.LocalDateTime

private val LOGGER = KotlinLogging.logger {}

fun Route.MinsteinntektApi() {

    val ulidGenerator = ULID()
    route("/minsteinntekt") {
        post {
            val parametere = call.receive<MinsteinntektInnParametere>()

            val minsteinntektBeregning = MinsteinntektBeregning(
                beregningsId = ulidGenerator.nextULID(),
                opprettet = LocalDateTime.now(),
                utfort = LocalDateTime.now(),
                parametere = MinsteinntektResultatParametere(
                    aktorId = parametere.aktorId,
                    vedtakId = parametere.vedtakId,
                    beregningsdato = LocalDate.now(),
                    inntektsId = ulidGenerator.nextULID()
                ),
                resultat = MinsteinntektResultat(
                    oppfyllerKravTilMinsteArbeidsinntekt = true,
                    periodeAntallUker = 52
                ),
                inntekt = setOf(
                    Inntekt(
                        inntekt = 2899182,
                        periode = 1,
                        inneholderNaeringsinntekter = false,
                        andel = 39982
                    ),
                    Inntekt(
                        inntekt = 2899182,
                        periode = 2,
                        inneholderNaeringsinntekter = false,
                        andel = 39982
                    ),
                    Inntekt(
                        inntekt = 2899182,
                        periode = 3,
                        inneholderNaeringsinntekter = false,
                        andel = 39982
                    )

                )

            )
            call.respond(HttpStatusCode.OK, minsteinntektBeregning)
        }
    }
}