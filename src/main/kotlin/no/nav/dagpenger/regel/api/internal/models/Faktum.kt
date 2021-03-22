package no.nav.dagpenger.regel.api.internal.models

import no.nav.dagpenger.regel.api.internal.RegelKontekst
import java.time.LocalDate

data class Faktum(
    val aktorId: String,
    val regelkontekst: RegelKontekst,
    val beregningsdato: LocalDate,
    val regelverksdato: LocalDate? = null,
    val inntektsId: String? = null,
    val inntektAvvik: Boolean? = null,
    val inntektManueltRedigert: Boolean? = null,
    val harAvtjentVerneplikt: Boolean? = null,
    val oppfyllerKravTilFangstOgFisk: Boolean? = null,
    val lærling: Boolean? = null,
    val antallBarn: Int? = null,
    val manueltGrunnlag: Int? = null,
    val bruktInntektsPeriode: InntektsPeriode? = null
)
