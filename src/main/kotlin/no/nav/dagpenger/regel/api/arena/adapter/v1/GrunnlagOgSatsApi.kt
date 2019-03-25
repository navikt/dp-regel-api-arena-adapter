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
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.internal.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.internal.models.GrunnlagFaktum
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
            var grunnlagOgSatsSubsumsjon: GrunnlagOgSatsSubsumsjon

            if (parametere.grunnlag == null) {
                val grunnlagSubsumsjon = synchronousGrunnlag.getGrunnlagSynchronously(parametere)

                parametere.grunnlag = grunnlagSubsumsjon.resultat.avkortet

                val satsSubsumsjon = synchronousSats.getSatsSynchronously(parametere)

                grunnlagOgSatsSubsumsjon =
                    mergeGrunnlagOgSatsSubsumsjon(
                        grunnlagSubsumsjon,
                        satsSubsumsjon
                    )
            } else {
                val satsSubsumsjon = synchronousSats.getSatsSynchronously(parametere)

                grunnlagOgSatsSubsumsjon =
                    mapGrunnlagOgSatsSubsumsjon(satsSubsumsjon)
            }

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
            satsFaktum.grunnlag
        ),
        GrunnlagOgSatsResultat(
            Grunnlag(
                grunnlagResultat.avkortet,
                grunnlagResultat.uavkortet
            ),
            Sats(satsResultat.dagsats, satsResultat.ukesats),
            GrunnlagOgSatsResultat.Beregningsregel.VERNEPLIKT,
            false
        ),
        grunnlagSubsumsjon.inntekt.map {
            Inntekt(
                it.inntekt,
                it.periode,
                InntektsPeriode(
                    it.inntektsPeriode.førsteMåned,
                    it.inntektsPeriode.sisteMåned
                ),
                it.inneholderFangstOgFisk,
                it.andel
            )
        }.toSet()
    )
}

fun mapGrunnlagOgSatsSubsumsjon(
    satsSubsumsjon: SatsSubsumsjon
): GrunnlagOgSatsSubsumsjon {

    val satsFaktum = satsSubsumsjon.faktum
    val satsResultat = satsSubsumsjon.resultat

    return GrunnlagOgSatsSubsumsjon(
        satsSubsumsjonsId = satsSubsumsjon.subsumsjonsId,
        opprettet = satsSubsumsjon.opprettet,
        utfort = satsSubsumsjon.utfort,
        parametere = GrunnlagOgSatsRegelFaktum(
            satsFaktum.aktorId,
            satsFaktum.vedtakId,
            satsFaktum.beregningsdato,
            antallBarn = satsFaktum.antallBarn,
            grunnlag = satsFaktum.grunnlag
        ),
        resultat = GrunnlagOgSatsResultat(
            sats = Sats(
                satsResultat.dagsats,
                satsResultat.ukesats
            ),
            beregningsRegel = GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G,
            benyttet90ProsentRegel = false
        )
    )
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