package no.nav.dagpenger.regel.api.arena.adapter.v2

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v2.models.Sats
import no.nav.dagpenger.regel.api.internalV2.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internalV2.BehovRequest
import no.nav.dagpenger.regel.api.internalV2.RegelApiBehovHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiStatusHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiSubsumsjonHttpClient
import no.nav.dagpenger.regel.api.internalV2.models.Subsumsjon
import java.time.LocalDateTime

fun Route.GrunnlagOgSatsApiV2(
    behovHttpClient: RegelApiBehovHttpClient,
    statusHttpClient: RegelApiStatusHttpClient,
    subsumsjonHttpClient: RegelApiSubsumsjonHttpClient
) {

    route("/dagpengegrunnlag") {
        post {
            val parametere = call.receive<GrunnlagOgSatsParametere>()

            val behovRequest = BehovRequest(
                parametere.aktorId,
                parametere.vedtakId,
                parametere.beregningsdato,
                harAvtjentVerneplikt = parametere.harAvtjentVerneplikt,
                oppfyllerKravTilFangstOgFisk = parametere.oppfyllerKravTilFangstOgFisk,
                manueltGrunnlag = parametere.grunnlag,
                antallBarn = parametere.antallBarn
            )

            val opprettet = LocalDateTime.now()

            val statusUrl = behovHttpClient.run(behovRequest)
            val subsumsjonLocation = statusHttpClient.pollStatus(statusUrl)
            val subsumsjon = subsumsjonHttpClient.getSubsumsjon(subsumsjonLocation)

            val utfort = LocalDateTime.now()

            val grunnlagOgSatsSubsumsjon = extractGrunnlagOgSats(subsumsjon, opprettet, utfort)

            call.respond(HttpStatusCode.OK, grunnlagOgSatsSubsumsjon)
        }
    }
}

private fun extractGrunnlagOgSats(
    subsumsjon: Subsumsjon,
    opprettet: LocalDateTime,
    utfort: LocalDateTime
): GrunnlagOgSatsSubsumsjon {

    val faktum = subsumsjon.faktum
    val grunnlagResultat = subsumsjon.grunnlagResultat ?: throw MissingSubsumsjonDataException("Missing grunnlagResultat")
    val satsResultat = subsumsjon.satsResultat ?: throw MissingSubsumsjonDataException("Missing satsResultat")

    return GrunnlagOgSatsSubsumsjon(
        grunnlagSubsumsjonsId = grunnlagResultat.subsumsjonsId,
        satsSubsumsjonsId = satsResultat.subsumsjonsId,
        opprettet = opprettet,
        utfort = utfort,
        parametere = GrunnlagOgSatsRegelFaktum(
            aktorId = faktum.aktorId,
            vedtakId = faktum.vedtakId,
            beregningsdato = faktum.beregningsdato,
            inntektsId = faktum.inntektsId,
            harAvtjentVerneplikt = faktum.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = faktum.oppfyllerKravTilFangstOgFisk,
            antallBarn = faktum.antallBarn ?: throw MissingSubsumsjonDataException("Missing faktum antallBarn"),
            grunnlag = faktum.manueltGrunnlag
        ),
        resultat = GrunnlagOgSatsResultat(
            grunnlag = Grunnlag(
                avkortet = grunnlagResultat.avkortet.toInt(),
                uavkortet = grunnlagResultat.uavkortet.toInt()
            ),
            sats = Sats(satsResultat.dagsats, satsResultat.ukesats),
            beregningsRegel = findBeregningsregel(grunnlagResultat),
            benyttet90ProsentRegel = satsResultat.benyttet90ProsentRegel
        ),
        inntekt = grunnlagResultat.grunnlagInntektsPerioder.map {
            Inntekt(
                inntekt = it.inntekt,
                periode = it.periode,
                inntektsPeriode = InntektsPeriode(
                    foersteMaaned = it.inntektsPeriode.førsteMåned,
                    sisteMaaned = it.inntektsPeriode.sisteMåned
                ),
                inneholderNaeringsinntekter = it.inneholderFangstOgFisk
            )
        }.toSet(),
        inntektManueltRedigert = faktum.inntektManueltRedigert,
        inntektAvvik = faktum.inntektAvvik
    )
}

private fun findBeregningsregel(grunnlagResultat: GrunnlagResultat): GrunnlagOgSatsResultat.Beregningsregel {

    return when {
        grunnlagResultat.beregningsregel == "Manuell" && grunnlagResultat.harAvkortet -> GrunnlagOgSatsResultat.Beregningsregel.MANUELL_OVER_6G
        grunnlagResultat.beregningsregel == "Manuell" -> GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste12" && grunnlagResultat.harAvkortet -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste12" -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_ETTAAR
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste12" && grunnlagResultat.harAvkortet -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_SISTE_2019
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste12" -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_ETTAAR
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste36" && grunnlagResultat.harAvkortet -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste36" -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_TREAAR
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste36" && grunnlagResultat.harAvkortet -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste36" -> GrunnlagOgSatsResultat.Beregningsregel.ORDINAER_TREAAR
        grunnlagResultat.beregningsregel == "Verneplikt" -> GrunnlagOgSatsResultat.Beregningsregel.VERNEPLIKT
        grunnlagResultat.beregningsregel == "Manuell under 6G" -> GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G
        grunnlagResultat.beregningsregel == "Manuell over 6G" -> GrunnlagOgSatsResultat.Beregningsregel.MANUELL_OVER_6G
        else -> throw FeilBeregningsregelException("Ukjent beregningsregel: '${grunnlagResultat.beregningsregel}'")
    }
}