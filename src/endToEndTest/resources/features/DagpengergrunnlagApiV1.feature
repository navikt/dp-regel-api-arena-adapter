#language: no
Egenskap: Hent dagpengergrunnlag

  @ignored
  Scenario: Beregn grunnlag og sats.
    Gitt at søker med aktør id "1234" med vedtak id 1234 med beregningsdato "2019-02-02" i beregning av grunnlag
    Når digidag skal beregne grunnlag
    Så er vedtak id 1234
