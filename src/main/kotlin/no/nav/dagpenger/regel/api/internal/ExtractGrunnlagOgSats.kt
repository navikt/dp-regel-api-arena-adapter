package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.regel.api.arena.adapter.v1.FeilBeregningsregelException
import no.nav.dagpenger.regel.api.arena.adapter.v1.MissingSubsumsjonDataException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NegativtGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NullGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import java.time.LocalDateTime

fun extractGrunnlagOgSats(
    subsumsjon: Subsumsjon,
    opprettet: LocalDateTime,
    utfort: LocalDateTime
): GrunnlagOgSatsSubsumsjon {

    val faktum = subsumsjon.faktum
    val grunnlagResultat =
        subsumsjon.grunnlagResultat ?: throw MissingSubsumsjonDataException("Missing grunnlagResultat")

    if (grunnlagResultat.erNegativt()) {
        throw NegativtGrunnlagException("Negativt grunnlag")
    }

    if (grunnlagResultat.erNull() && faktum.lærling == true) { // @todo : Burde sjekken være for alle parameterene, ikke bare for lærling
        throw NullGrunnlagException("Grunnlaget er 0")
    }

    val satsResultat = subsumsjon.satsResultat ?: throw MissingSubsumsjonDataException("Missing satsResultat")

    return GrunnlagOgSatsSubsumsjon(
        grunnlagSubsumsjonsId = grunnlagResultat.subsumsjonsId,
        satsSubsumsjonsId = satsResultat.subsumsjonsId,
        opprettet = opprettet,
        utfort = utfort,
        parametere = GrunnlagOgSatsRegelFaktum(
            aktorId = faktum.aktorId,
            vedtakId = faktum.regelkontekst.id.toInt(),
            beregningsdato = faktum.beregningsdato,
            regelverksdato = faktum.regelverksdato,
            inntektsId = faktum.inntektsId,
            harAvtjentVerneplikt = faktum.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = faktum.oppfyllerKravTilFangstOgFisk,
            oppfyllerKravTilLaerling = faktum.lærling,
            antallBarn = faktum.antallBarn ?: throw MissingSubsumsjonDataException("Missing faktum antallBarn"),
            grunnlag = faktum.manueltGrunnlag,
            manueltGrunnlag = faktum.manueltGrunnlag,
            forrigeGrunnlag = faktum.forrigeGrunnlag
        ),
        resultat = GrunnlagOgSatsResultat(
            grunnlag = Grunnlag(
                avkortet = grunnlagResultat.avkortet.toInt(),
                uavkortet = grunnlagResultat.uavkortet.toInt(),
                beregningsregel = findBeregningsregel(grunnlagResultat.beregningsregel, grunnlagResultat.harAvkortet)
            ),
            sats = Sats(
                dagsats = satsResultat.dagsats,
                ukesats = satsResultat.ukesats,
                beregningsregel = satsResultat.beregningsregel
            ),
            benyttet90ProsentRegel = satsResultat.benyttet90ProsentRegel
        ),
        inntekt = grunnlagResultat.grunnlagInntektsPerioder?.map {
            Inntekt(
                inntekt = it.inntekt.round().toInt(),
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

fun findBeregningsregel(beregningsregel: String, harAvkortet: Boolean): GrunnlagBeregningsregel {
    return when {
        beregningsregel == "Manuell" && harAvkortet -> GrunnlagBeregningsregel.MANUELL_OVER_6G
        beregningsregel == "Manuell" -> GrunnlagBeregningsregel.MANUELL_UNDER_6G
        beregningsregel == "ForrigeGrunnlag" -> GrunnlagBeregningsregel.FORRIGE_GRUNNLAG // Brukes ikke av Arena
        beregningsregel == "ArbeidsinntektSiste12" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019
        beregningsregel == "ArbeidsinntektSiste12" -> GrunnlagBeregningsregel.ORDINAER_ETTAAR
        beregningsregel == "FangstOgFiskSiste12" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019
        beregningsregel == "FangstOgFiskSiste12" -> GrunnlagBeregningsregel.ORDINAER_ETTAAR
        beregningsregel == "ArbeidsinntektSiste36" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        beregningsregel == "ArbeidsinntektSiste36" -> GrunnlagBeregningsregel.ORDINAER_TREAAR
        beregningsregel == "FangstOgFiskSiste36" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        beregningsregel == "FangstOgFiskSiste36" -> GrunnlagBeregningsregel.ORDINAER_TREAAR
        beregningsregel == "Verneplikt" -> GrunnlagBeregningsregel.VERNEPLIKT
        beregningsregel == "LærlingFangstOgFisk1x12" && harAvkortet -> GrunnlagBeregningsregel.LAERLING_12_MAANED_AVKORTET
        beregningsregel == "LærlingFangstOgFisk3x4" && harAvkortet -> GrunnlagBeregningsregel.LAERLING_4_MAANED_AVKORTET
        beregningsregel == "LærlingArbeidsinntekt1x12" && harAvkortet -> GrunnlagBeregningsregel.LAERLING_12_MAANED_AVKORTET
        beregningsregel == "LærlingArbeidsinntekt3x4" && harAvkortet -> GrunnlagBeregningsregel.LAERLING_4_MAANED_AVKORTET
        beregningsregel == "LærlingFangstOgFisk1x12" -> GrunnlagBeregningsregel.LAERLING_12_MAANED
        beregningsregel == "LærlingFangstOgFisk3x4" -> GrunnlagBeregningsregel.LAERLING_4_MAANED
        beregningsregel == "LærlingArbeidsinntekt1x12" -> GrunnlagBeregningsregel.LAERLING_12_MAANED
        beregningsregel == "LærlingArbeidsinntekt3x4" -> GrunnlagBeregningsregel.LAERLING_4_MAANED
        else -> throw FeilBeregningsregelException("Ukjent beregningsregel: '$beregningsregel'")
    }
}
