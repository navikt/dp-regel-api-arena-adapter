package no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag

class DagpengegrunnlagResultat(
    val grunnlag: Dagpengegrunnlag,
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
