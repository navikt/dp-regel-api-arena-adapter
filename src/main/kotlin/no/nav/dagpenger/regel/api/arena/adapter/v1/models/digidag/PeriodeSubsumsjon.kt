package no.nav.dagpenger.regel.api.arena.adapter.v1.models.digidag

import java.time.LocalDate
import java.time.LocalDateTime

data class PeriodeSubsumsjon(
    val subsumsjonsId: String,
    val opprettet: LocalDateTime,
    val utfort: LocalDateTime,
    val faktum: PeriodeFaktum,
    val resultat: PeriodeResultat,
    val inntekt: Set<Inntekt>
)

data class PeriodeResultat(
    val antallUker: Int
)

data class PeriodeFaktum(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean? = false,
    val oppfyllerKravTilFangstOgFisk: Boolean? = false,
    val bruktInntektsPeriode: InntektsPeriode? = null
)
