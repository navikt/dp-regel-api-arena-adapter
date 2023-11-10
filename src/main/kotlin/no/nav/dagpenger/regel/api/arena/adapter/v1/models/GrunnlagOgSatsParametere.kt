package no.nav.dagpenger.regel.api.arena.adapter.v1.models

import java.time.LocalDate

data class GrunnlagOgSatsParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val oppfyllerKravTilLaerling: Boolean = false,
    val antallBarn: Int = 0,
    val grunnlag: Int? = null,
    val manueltGrunnlag: Int? = null,
    val forrigeGrunnlag: Int? = null,
    val regelverksdato: LocalDate = beregningsdato,
)
