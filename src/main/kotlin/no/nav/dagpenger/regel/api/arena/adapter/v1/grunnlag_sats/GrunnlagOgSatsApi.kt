package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.SynchronousGrunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SynchronousSats
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.arena.Sats
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.GrunnlagFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.SatsFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag.SatsSubsumsjon

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
        grunnlagSubsumsjon.inntekt.map { Inntekt(it.inntekt, it.periode, InntektsPeriode(it.inntektsPeriode.foersteMaaned, it.inntektsPeriode.sisteMaaned), it.inneholderFangstOgFisk, it.andel) }
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
        grunnlagFaktum.beregningsdato.equals(satsFaktum.beregningsdato)) {
        return true
    }
    return false
}

class UnMatchingFaktumException(override val message: String) : RuntimeException(message)
