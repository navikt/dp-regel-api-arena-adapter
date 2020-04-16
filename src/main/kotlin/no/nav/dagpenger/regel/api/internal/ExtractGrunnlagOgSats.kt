package no.nav.dagpenger.regel.api.internal

import java.time.LocalDateTime
import no.nav.dagpenger.regel.api.arena.adapter.v1.FeilBeregningsregelException
import no.nav.dagpenger.regel.api.arena.adapter.v1.MissingSubsumsjonDataException
import no.nav.dagpenger.regel.api.arena.adapter.v1.NegativtGrunnlagException
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Grunnlag
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagBeregningsregel
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsRegelFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsResultat
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Inntekt
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.InntektsPeriode
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.Sats
import no.nav.dagpenger.regel.api.internal.models.GrunnlagResultat
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon

fun extractGrunnlagOgSats(
    subsumsjon: Subsumsjon,
    opprettet: LocalDateTime,
    utfort: LocalDateTime,
    koronaToggle: Boolean
): GrunnlagOgSatsSubsumsjon {

    val faktum = subsumsjon.faktum
    val grunnlagResultat =
        subsumsjon.grunnlagResultat ?: throw MissingSubsumsjonDataException("Missing grunnlagResultat")

    if (grunnlagResultat.erNegativt()) {
        throw NegativtGrunnlagException("Negativt grunnlag")
    }

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
                uavkortet = grunnlagResultat.uavkortet.toInt(),
                beregningsregel = when (koronaToggle) {
                    true -> findBeregningsregel(
                        grunnlagResultat.beregningsregel,
                        grunnlagResultat.harAvkortet
                    )
                    else -> null
                }
            ),
            sats = Sats(
                dagsats = satsResultat.dagsats,
                ukesats = satsResultat.ukesats,
                beregningsregel = when (koronaToggle) {
                    true -> satsResultat.beregningsregel
                    else -> null
                }
            ),
            beregningsRegel = when (koronaToggle) {
                false -> findBeregningsregel(grunnlagResultat.beregningsregel, grunnlagResultat.harAvkortet)
                else -> null
            },
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
        beregningsregel == "ArbeidsinntektSiste12" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019
        beregningsregel == "ArbeidsinntektSiste12" -> GrunnlagBeregningsregel.ORDINAER_ETTAAR
        beregningsregel == "FangstOgFiskSiste12" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_SISTE_2019
        beregningsregel == "FangstOgFiskSiste12" -> GrunnlagBeregningsregel.ORDINAER_ETTAAR
        beregningsregel == "ArbeidsinntektSiste36" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        beregningsregel == "ArbeidsinntektSiste36" -> GrunnlagBeregningsregel.ORDINAER_TREAAR
        beregningsregel == "FangstOgFiskSiste36" && harAvkortet -> GrunnlagBeregningsregel.ORDINAER_OVER_6G_3SISTE_2019
        beregningsregel == "FangstOgFiskSiste36" -> GrunnlagBeregningsregel.ORDINAER_TREAAR
        beregningsregel == "Verneplikt" -> GrunnlagBeregningsregel.VERNEPLIKT
        beregningsregel == "Manuell under 6G" -> GrunnlagBeregningsregel.MANUELL_UNDER_6G
        beregningsregel == "Manuell over 6G" -> GrunnlagBeregningsregel.MANUELL_OVER_6G
        else -> throw FeilBeregningsregelException("Ukjent beregningsregel: '$beregningsregel'")
    }
}

fun GrunnlagResultat.erNegativt() = this.uavkortet.toInt() < 0
