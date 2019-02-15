package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag_sats

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.InntektsPeriode
import java.time.LocalDate

data class GrunnlagOgSatsParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    var grunnlag: Int? = null
)
