package no.nav.dagpenger.regel.api.arena.adapter.v1

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektGrunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.internal.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.internal.models.GrunnlagFaktum
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internal.models.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.SatsFaktum
import no.nav.dagpenger.regel.api.internal.models.SatsSubsumsjon
import no.nav.dagpenger.regel.api.internal.sats.SynchronousSats

fun Route.GrunnlagOgSatsApi(
    synchronousGrunnlag: SynchronousGrunnlag,
    synchronousSats: SynchronousSats
) {

    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()

            val grunnlagSubsumsjon = synchronousGrunnlag.getGrunnlagSynchronously(parametere)

            val satsSubsumsjon = synchronousSats.getSatsSynchronously(parametere)

            val grunnlagOgSatsSubsumsjon =
                mergeGrunnlagOgSatsSubsumsjon(
                    grunnlagSubsumsjon,
                    satsSubsumsjon
                )

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }
}

fun mergeGrunnlagOgSatsSubsumsjon(
    grunnlagSubsumsjon: GrunnlagSubsumsjon,
    satsSubsumsjon: SatsSubsumsjon
): GrunnlagOgSatsSubsumsjon {

    val grunnlagFaktum = grunnlagSubsumsjon.faktum
    val grunnlagResultat = grunnlagSubsumsjon.resultat

    val satsFaktum = satsSubsumsjon.faktum
    val satsResultat = satsSubsumsjon.resultat

    if (!compareFields(
            grunnlagFaktum,
            satsFaktum
        )
    ) throw UnMatchingFaktumException("Grunnlag and sats faktum dont match")

    return GrunnlagOgSatsSubsumsjon(
        grunnlagSubsumsjon.subsumsjonsId,
        satsSubsumsjon.subsumsjonsId,
        grunnlagSubsumsjon.opprettet,
        grunnlagSubsumsjon.utfort,
        GrunnlagOgSatsRegelFaktum(
            grunnlagFaktum.aktorId,
            grunnlagFaktum.vedtakId,
            grunnlagFaktum.beregningsdato,
            grunnlagFaktum.inntektsId,
            grunnlagFaktum.harAvtjentVerneplikt,
            grunnlagFaktum.oppfyllerKravTilFangstOgFisk,
            satsFaktum.antallBarn,
            grunnlagFaktum.manueltGrunnlag
        ),
        GrunnlagOgSatsResultat(
            Grunnlag(
                grunnlagResultat.avkortet,
                grunnlagResultat.uavkortet
            ),
            Sats(satsResultat.dagsats, satsResultat.ukesats),
            findBeregningsregel(grunnlagResultat),
            satsResultat.benyttet90ProsentRegel
        ),
        grunnlagSubsumsjon.inntekt.map {
            InntektGrunnlag(
                it.inntekt,
                it.periode,
                InntektsPeriode(
                    it.inntektsPeriode.førsteMåned,
                    it.inntektsPeriode.sisteMåned
                ),
                it.inneholderFangstOgFisk
            )
        }.toSet()
    )
}

fun findBeregningsregel(grunnlagResultat: GrunnlagResultat): GrunnlagOgSatsResultat.Beregningsregel {
    val beregningsregel = grunnlagResultat.beregningsregel
    return when (beregningsregel) {
        "ArbeidsinntektSiste12", "FangstOgFiskSiste12" -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019
        "ArbeidsinntektSiste36", "FangstOgFiskSiste36" -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        "Verneplikt" -> GrunnlagOgSatsResultat.Beregningsregel.VERNEPLIKT
        "Manuell under 6G" -> GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G
        "Manuell over 6G" -> GrunnlagOgSatsResultat.Beregningsregel.MANUELL_OVER_6G
        else -> throw FeilBeregningsregelException("Ukjent beregningsregel: '$beregningsregel'")
    }
}

fun compareFields(grunnlagFaktum: GrunnlagFaktum, satsFaktum: SatsFaktum): Boolean {

    if (grunnlagFaktum.aktorId.equals(satsFaktum.aktorId) &&
        grunnlagFaktum.vedtakId.equals(satsFaktum.vedtakId) &&
        grunnlagFaktum.beregningsdato.equals(satsFaktum.beregningsdato)
    ) {
        return true
    }
    return false
}

class FeilBeregningsregelException(message: String) : RuntimeException(message)
