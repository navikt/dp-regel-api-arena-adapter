package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.Dagpengegrunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.DagpengegrunnlagBeregning
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.DagpengegrunnlagInnParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.DagpengegrunnlagResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.DagpengegrunnlagResultatParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag.Sats
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

fun Route.DagpengegrunnlagApi() {

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
                    beregningsregel = DagpengegrunnlagResultat.Beregningsregel.ORDINAER_ETTAAR,
                    benyttet90ProsentRegel = false
                ),
                inntekt = setOf(
                    Inntekt(
                        inntekt = 2899182,
                        inntektsPeriode = InntektsPeriode(
                            sisteMaaned = YearMonth.of(2019, 2),
                            foersteMaaned = YearMonth.of(2018, 2)
                        ),
                        periode = 1,
                        inneholderNaeringsinntekter = false,
                        andel = 39982
                    ),
                    Inntekt(
                        inntekt = 2899182,
                        periode = 2,
                        inntektsPeriode = InntektsPeriode(
                            sisteMaaned = YearMonth.of(2018, 3),
                            foersteMaaned = YearMonth.of(2017, 2)
                        ),
                        inneholderNaeringsinntekter = false,
                        andel = 39982
                    ),
                    Inntekt(
                        inntekt = 2899182,
                        periode = 3,
                        inntektsPeriode = InntektsPeriode(
                            sisteMaaned = YearMonth.of(2017, 3),
                            foersteMaaned = YearMonth.of(2016, 2)
                        ),
                        inneholderNaeringsinntekter = false,
                        andel = 39982
                    )

                )

            )
            call.respond(HttpStatusCode.OK, dagpengegrunnlagBeregning)
        }
    }
}