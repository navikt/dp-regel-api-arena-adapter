package no.nav.dagpenger.regel.api.internalV2

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.arena.adapter.v2.FeilBeregningsregelException
import no.nav.dagpenger.regel.api.arena.adapter.v2.MissingSubsumsjonDataException
import no.nav.dagpenger.regel.api.internalV2.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internalV2.models.Subsumsjon
import java.time.LocalDateTime

fun extractGrunnlagOgSats(
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
        inntekt = grunnlagResultat.grunnlagInntektsPerioder?.map {
            Inntekt(
                inntekt = it.inntekt,
                periode = it.periode,
                inntektsPeriode = InntektsPeriode(
                    foersteMaaned = it.inntektsPeriode.førsteMåned,
                    sisteMaaned = it.inntektsPeriode.sisteMåned
                ),
                inneholderNaeringsinntekter = it.inneholderFangstOgFisk
            )
        }?.toSet(),
        inntektManueltRedigert = faktum.inntektManueltRedigert,
        inntektAvvik = faktum.inntektAvvik
    )
}

private fun findBeregningsregel(grunnlagResultat: GrunnlagResultat): GrunnlagBeregningsregel {

    return when {
        grunnlagResultat.beregningsregel == "Manuell" && grunnlagResultat.harAvkortet -> GrunnlagBeregningsregel.MANUELL_OVER_6G
        grunnlagResultat.beregningsregel == "Manuell" -> GrunnlagBeregningsregel.MANUELL_UNDER_6G
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste12" && grunnlagResultat.harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste12" -> GrunnlagBeregningsregel.ORDINAER_ETTAAR
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste12" && grunnlagResultat.harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste12" -> GrunnlagBeregningsregel.ORDINAER_ETTAAR
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste36" && grunnlagResultat.harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        grunnlagResultat.beregningsregel == "ArbeidsinntektSiste36" -> GrunnlagBeregningsregel.ORDINAER_TREAAR
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste36" && grunnlagResultat.harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        grunnlagResultat.beregningsregel == "FangstOgFiskSiste36" -> GrunnlagBeregningsregel.ORDINAER_TREAAR
        grunnlagResultat.beregningsregel == "Verneplikt" -> GrunnlagBeregningsregel.VERNEPLIKT
        grunnlagResultat.beregningsregel == "Manuell under 6G" -> GrunnlagBeregningsregel.MANUELL_UNDER_6G
        grunnlagResultat.beregningsregel == "Manuell over 6G" -> GrunnlagBeregningsregel.MANUELL_OVER_6G
        else -> throw FeilBeregningsregelException("Ukjent beregningsregel: '${grunnlagResultat.beregningsregel}'")
    }
}