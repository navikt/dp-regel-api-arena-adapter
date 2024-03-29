package no.nav.dagpenger.regel.api.arena.adapter.v1.models

import java.time.LocalDate
import java.time.LocalDateTime

data class GrunnlagOgSatsSubsumsjon(
    val grunnlagSubsumsjonsId: String? = null,
    val satsSubsumsjonsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: GrunnlagOgSatsRegelFaktum,
    val resultat: GrunnlagOgSatsResultat,
    val inntekt: Set<Inntekt>? = null,
    val inntektManueltRedigert: Boolean? = null,
    val inntektAvvik: Boolean? = null,
)

data class GrunnlagOgSatsRegelFaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate? = null,
    val regelverksdato: LocalDate? = null,
    val inntektsId: String? = null,
    val harAvtjentVerneplikt: Boolean? = null,
    val oppfyllerKravTilFangstOgFisk: Boolean? = null,
    val oppfyllerKravTilLaerling: Boolean? = null,
    val antallBarn: Int,
    val grunnlag: Int?,
    val manueltGrunnlag: Int?,
    val forrigeGrunnlag: Int?,
)

data class GrunnlagOgSatsResultat(
    val grunnlag: Grunnlag? = null,
    val sats: Sats,
    val benyttet90ProsentRegel: Boolean,
)

enum class GrunnlagBeregningsregel {
    ORDINAER_OVER_6G_SISTE_2019,
    ORDINAER_OVER_6G_3SISTE_2019,
    ORDINAER_OVER_6G_SISTE_2021,
    ORDINAER_OVER_6G_3SISTE_2021,
    ORDINAER_ETTAAR,
    ORDINAER_TREAAR,
    MANUELL_UNDER_6G,
    MANUELL_OVER_6G,
    FORRIGE_GRUNNLAG,
    VERNEPLIKT,
    LAERLING_12_MAANED,
    LAERLING_12_MAANED_AVKORTET,
    LAERLING_4_MAANED,
    LAERLING_4_MAANED_AVKORTET,
}

data class Grunnlag(
    val avkortet: Int,
    val uavkortet: Int? = null,
    val beregningsregel: GrunnlagBeregningsregel? = null,
)

enum class SatsBeregningsregel {
    ORDINAER,
    KORONA,
    KORONA_LAERLING,
}

data class Sats(
    val dagsats: Int,
    val ukesats: Int,
    val beregningsregel: SatsBeregningsregel? = null,
)
