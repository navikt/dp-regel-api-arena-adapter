package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import de.huxhorn.sulky.ulid.ULID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import java.time.LocalDate
import java.time.LocalDateTime

fun Route.GrunnlagOgSatsApi() {

    val ulidGenerator = ULID()
    route("/dagpengegrunnlag") {
        post {
            val dagpengegrunnlagInnParametere = call.receive<DagpengegrunnlagInnParametere>()

            val dagpengegrunnlagBeregning = DagpengegrunnlagBeregning(
                beregningsId = ulidGenerator.nextULID(),
                opprettet = LocalDateTime.now(),
                utfort = LocalDateTime.now(),
                parametere = DagpengegrunnlagResultatParametere(
                    aktorId = dagpengegrunnlagInnParametere.aktorId,
                    vedtakId = dagpengegrunnlagInnParametere.vedtakId,
                    beregningsdato = LocalDate.now(),
                    inntektsId = ulidGenerator.nextULID()
                ),
                resultat = DagpengegrunnlagResultat(
                    grunnlag = Dagpengegrunnlag(
                        avkortet = 342352,
                        uavkortet = 342352
                    ),
                    sats = Sats(
                        dagsats = 213,
                        ukesats = 1065
                    ),
                    beregningsRegel = DagpengegrunnlagResultat.Beregningsregel.ORDINAER_ETTAAR,
                    benyttet90ProsentRegel = false
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
            call.respond(HttpStatusCode.OK, dagpengegrunnlagBeregning)
        }
    }
}