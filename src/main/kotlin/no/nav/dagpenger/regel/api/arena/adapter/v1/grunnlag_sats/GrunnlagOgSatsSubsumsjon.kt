package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.grunnlag.GrunnlagSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats.sats.SatsSubsumsjon
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import java.time.LocalDate
import java.time.LocalDateTime

data class GrunnlagOgSatsSubsumsjon(
    val beregningsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: GrunnlagOgSatsRegelFaktum,
    val resultat: GrunnlagOgSatsResultat,
    val inntekt: Set<Inntekt>

)

data class GrunnlagOgSatsRegelFaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String,
    val harAvtjentVerneplikt: Boolean,
    val oppfyllerKravTilFangstOgFisk: Boolean,
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
    val uavkortet: Int
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

    return GrunnlagOgSatsSubsumsjon(
        grunnlagSubsumsjon.subsumsjonsId,
        grunnlagSubsumsjon.opprettet,
        grunnlagSubsumsjon.utfort,
        GrunnlagOgSatsRegelFaktum(
            grunnlagFaktum.aktorId,
            grunnlagFaktum.vedtakId,
            grunnlagFaktum.beregningsdato,
            grunnlagFaktum.inntektsId,
            grunnlagFaktum.harAvtjentVerneplikt,
            grunnlagFaktum.oppfyllerKravTilFangstOgFisk,
            grunnlagFaktum.antallBarn,
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
