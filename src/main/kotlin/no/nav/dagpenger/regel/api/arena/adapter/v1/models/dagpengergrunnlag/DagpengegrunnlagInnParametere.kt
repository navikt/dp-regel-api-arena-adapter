package no.nav.dagpenger.regel.api.arena.adapter.v1.models.dagpengergrunnlag

import no.nav.dagpenger.regel.api.arena.adapter.v1.models.common.InntektsPeriode
import java.time.LocalDate

data class DagpengegrunnlagInnParametere(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsDato: LocalDate,
    val harAvtjentVerneplikt: Boolean = false,
    val oppfyllerKravTilFangstOgFisk: Boolean = false,
    val antallBarn: Int = 0,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val grunnlag: Int? = null
)

/** {
"beregningsId": "01ARZ3NDEKTSV4RRFFQ69G5FAV",
"opprettet": "2018-12-26T14:42:09Z",
"utfort": "2018-12-26T14:43:12Z",
"parametere": {
"aktorId": "01019955667",
"vedtakId": 1234,
"beregningsdato": "2019-01-11",
"inntektsId": "01D2FB328P06WD808P22N9CFY5",
"harAvtjentVerneplikt": false,
"oppfyllerKravTilFangstOgFisk": false,
"antallBarn": 0,
"bruktInntektsPeriode": {
"foersteMaaned": "2019-01",
"sisteMaaned": "2018-01"
},
"grunnlag": 0
},
"resultat": {
"grunnlag": {
"avkortet": 342352,
"uavkortet": 342352
},
"sats": {
"dagsats": 213,
"ukesats": 1065
},
"beregningsregel": "ORDINAER_ETTAAR"
}
} **/