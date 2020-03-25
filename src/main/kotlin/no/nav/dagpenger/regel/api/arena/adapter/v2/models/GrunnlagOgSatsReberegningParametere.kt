package no.nav.dagpenger.regel.api.arena.adapter.v2.models

import java.lang.RuntimeException
import java.time.LocalDate

data class GrunnlagOgSatsReberegningParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val inntektsId: String,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    var grunnlag: Int? = null
)

class IllegalInntektIdException(override val cause: Throwable?) : RuntimeException(cause)