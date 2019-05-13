package no.nav.dagpenger.regel.api.internal.models

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import java.time.LocalDate

data class SatsParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean = false,
    val manueltGrunnlag: Int? = null,
    val antallBarn: Int? = 0,
    val oppfyllerKravTilFangstOgFisk: Boolean = false
)

fun GrunnlagOgSatsParametere.toSatsParametere(): SatsParametere =
    SatsParametere(
        aktorId = this.aktorId,
        vedtakId = this.vedtakId,
        beregningsdato = this.beregningsdato,
        harAvtjentVerneplikt = this.harAvtjentVerneplikt,
        antallBarn = this.antallBarn,
        manueltGrunnlag = this.grunnlag,
        oppfyllerKravTilFangstOgFisk = this.oppfyllerKravTilFangstOgFisk
    )
