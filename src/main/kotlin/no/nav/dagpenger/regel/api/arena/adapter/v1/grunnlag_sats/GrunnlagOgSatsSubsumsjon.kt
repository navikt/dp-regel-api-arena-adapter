package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.GrunnlagFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SatsFaktum
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import java.time.LocalDate
import java.time.LocalDateTime

data class GrunnlagOgSatsSubsumsjon(
    val grunnlagSubsumsjonsId: String? = null,
    val satsSubsumsjonsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: GrunnlagOgSatsRegelFaktum,
    val resultat: GrunnlagOgSatsResultat,
    val inntekt: Set<Inntekt>? = null

)

data class GrunnlagOgSatsRegelFaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate? = null,
    val inntektsId: String? = null,
    val harAvtjentVerneplikt: Boolean? = null,
    val oppfyllerKravTilFangstOgFisk: Boolean? = null,
    val antallBarn: Int,
    val grunnlag: Int
)

class GrunnlagOgSatsResultat(
    val grunnlag: Grunnlag,
    val sats: Sats,
    val beregningsRegel: Beregningsregel,
    val benyttet90ProsentRegel: Boolean
) {
    enum class Beregningsregel {
        ORDINAER_ETTAAR,
        ORDINAER_TREAAR,
        ORDINAER_OVER_6G,
        ORDINAER_OVER_6G_SISTE_2019,
        ORDINAER_OVER_6G_3SISTE_2019,
        MANUELL_UNDER_6G,
        MANUELL_OVER_6G,
        VERNEPLIKT
    }
}

class Grunnlag(
    val avkortet: Int,
    val uavkortet: Int? = null
)

data class Sats(
    val dagsats: Int,
    val ukesats: Int
)

fun mergeGrunnlagOgSatsSubsumsjon(
    grunnlagSubsumsjon: GrunnlagSubsumsjon,
    satsSubsumsjon: SatsSubsumsjon
): GrunnlagOgSatsSubsumsjon {

    val grunnlagFaktum = grunnlagSubsumsjon.faktum
    val grunnlagResultat = grunnlagSubsumsjon.resultat

    val satsFaktum = satsSubsumsjon.faktum
    val satsResultat = satsSubsumsjon.resultat

    if (!compareFields(grunnlagFaktum, satsFaktum)) throw UnMatchingFaktumException("Grunnlag and sats faktum dont match")

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
            Grunnlag(grunnlagResultat.avkortet, grunnlagResultat.uavkortet),
            Sats(satsResultat.dagsats, satsResultat.ukesats),
            GrunnlagOgSatsResultat.Beregningsregel.VERNEPLIKT,
            false
        ),
        grunnlagSubsumsjon.inntekt
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
            Grunnlag(satsFaktum.grunnlag),
            Sats(satsResultat.dagsats, satsResultat.ukesats),
            GrunnlagOgSatsResultat.Beregningsregel.MANUELL_UNDER_6G,
            false
        )
    )
}

fun compareFields(grunnlagFaktum: GrunnlagFaktum, satsFaktum: SatsFaktum): Boolean {

    if (grunnlagFaktum.aktorId.equals(satsFaktum.aktorId) &&
        grunnlagFaktum.vedtakId.equals(satsFaktum.vedtakId) &&
        grunnlagFaktum.beregningsdato.equals(satsFaktum.beregningsdato) &&
        grunnlagFaktum.antallBarn.equals(satsFaktum.antallBarn)) {
        return true
    }
    return false
}

class UnMatchingFaktumException(override val message: String) : RuntimeException(message)