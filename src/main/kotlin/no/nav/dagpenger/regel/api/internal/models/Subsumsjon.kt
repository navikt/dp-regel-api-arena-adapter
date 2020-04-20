package no.nav.dagpenger.regel.api.internal.models

import java.math.BigDecimal
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.SatsBeregningsregel

data class Subsumsjon(
    val behovId: String,
    val faktum: Faktum,
    val grunnlagResultat: GrunnlagResultat?,
    val minsteinntektResultat: MinsteinntektResultat?,
    val periodeResultat: PeriodeResultat?,
    val satsResultat: SatsResultat?,
    val problem: Problem?
)

data class GrunnlagResultat(
    val subsumsjonsId: String,
    val sporingsId: String,
    val regelIdentifikator: String,
    val avkortet: BigDecimal,
    val uavkortet: BigDecimal,
    val harAvkortet: Boolean,
    val beregningsregel: String,
    val grunnlagInntektsPerioder: List<Inntekt>?

) {

    companion object {
        private val zero = BigDecimal(0)
    }

    fun erNegativt() = this.uavkortet < zero
    fun erNull() = this.uavkortet == zero
}

data class MinsteinntektResultat(
    val subsumsjonsId: String,
    val sporingsId: String,
    val oppfyllerMinsteinntekt: Boolean,
    val regelIdentifikator: String,
    val minsteinntektInntektsPerioder: List<Inntekt>,
    val beregningsregel: Beregningsregel
)

data class PeriodeResultat(
    val subsumsjonsId: String,
    val sporingsId: String,
    val regelIdentifikator: String,
    val periodeAntallUker: Int
)

data class SatsResultat(
    val subsumsjonsId: String,
    val sporingsId: String,
    val regelIdentifikator: String,
    val dagsats: Int,
    val ukesats: Int,
    val benyttet90ProsentRegel: Boolean,
    val beregningsregel: SatsBeregningsregel? = null
)

enum class Beregningsregel {
    ORDINAER, KORONA, KORONA_LAERLING
}
