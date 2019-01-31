package no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import java.time.LocalDateTime

data class MinsteinntektBeregning(
    val beregningsId: String,
    val opprettet: LocalDateTime, // todo: ZonedDateTime?
    val utfort: LocalDateTime, // todo: ZonedDateTime?,
    val parametere: MinsteinntektResultatParametere,
    val resultat: MinsteinntektResultat,
    val inntekt: Set<Inntekt>
)