package no.nav.dagpenger.regel.api.arena.adapter.v1.grunnlag

import org.intellij.lang.annotations.Language

@Language("JSON")
internal val expectedGrunnlagJson =
    """{
  "grunnlagSubsumsjonsId": "1234",
  "satsSubsumsjonsId": "4567",
  "opprettet": "2000-08-11T15:30:11",
  "utfort": "2000-08-11T15:30:11",
  "parametere": {
    "aktorId": "1234",
    "vedtakId": 123,
    "beregningsdato": "2019-02-10",
    "inntektsId": "1234",
    "harAvtjentVerneplikt": false,
    "oppfyllerKravTilFangstOgFisk": false,
    "antallBarn": 0,
    "grunnlag": 12345,
    "manueltGrunnlag": 12345
  },
  "resultat": {
    "grunnlag": {
      "avkortet": 12345,
      "uavkortet": 12345,
      "beregningsregel": "ORDINAER_ETTAAR"
    },
    "sats": {
      "dagsats": 124,
      "ukesats": 234
    },
    "benyttet90ProsentRegel": false
  },
  "inntekt": [
    {
      "inntekt": 4999423,
      "periode": 1,
      "inntektsPeriode": {
        "foersteMaaned": "2018-01",
        "sisteMaaned": "2019-01"
      },
      "inneholderNaeringsinntekter": false
    }
  ],
  "inntektManueltRedigert": true,
  "inntektAvvik": true
}"""

@Language("JSON")
internal val expectedGrunnlagJsonWithBeregningsregel =
    """{
  "grunnlagSubsumsjonsId": "1234",
  "satsSubsumsjonsId": "4567",
  "opprettet": "2000-08-11T15:30:11",
  "utfort": "2000-08-11T15:30:11",
  "parametere": {
    "aktorId": "1234",
    "vedtakId": 123,
    "beregningsdato": "2019-02-10",
    "inntektsId": "1234",
    "harAvtjentVerneplikt": false,
    "oppfyllerKravTilFangstOgFisk": false,
    "antallBarn": 0,
    "grunnlag": 12345,
    "manueltGrunnlag": 12345
  },
  "resultat": {
    "grunnlag": {
      "avkortet": 12345,
      "uavkortet": 12345,
      "beregningsregel": "ORDINAER_ETTAAR"
    },
    "sats": {
      "dagsats": 124,
      "ukesats": 234,
      "beregningsregel": "ORDINAER"
    },
    "benyttet90ProsentRegel": false
  },
  "inntekt": [
    {
      "inntekt": 4999423,
      "periode": 1,
      "inntektsPeriode": {
        "foersteMaaned": "2018-01",
        "sisteMaaned": "2019-01"
      },
      "inneholderNaeringsinntekter": false
    }
  ],
  "inntektManueltRedigert": true,
  "inntektAvvik": true
}"""
