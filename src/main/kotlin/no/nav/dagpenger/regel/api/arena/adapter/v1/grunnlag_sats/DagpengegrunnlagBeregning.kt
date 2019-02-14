package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.Inntekt
import java.time.LocalDateTime

data class DagpengegrunnlagBeregning(
    val beregningsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val parametere: DagpengegrunnlagResultatParametere,
    val resultat: DagpengegrunnlagResultat,
    val inntekt: Set<Inntekt>
)