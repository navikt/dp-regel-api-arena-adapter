package no.nav.dagpenger.regel.api.arena.adapter.v1.models.minsteinntekt

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.InntektsPeriode
import java.time.LocalDate

data class MinsteinntektResultatParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilFangstOgFisk: Boolean? = false,
    val bruktInntektsPeriode: InntektsPeriode? = null
)
