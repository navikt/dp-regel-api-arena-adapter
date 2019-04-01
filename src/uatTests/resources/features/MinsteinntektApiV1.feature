#language: no
Egenskap: Vurderer minsteinntektskrav
  @ignored
  Scenario: Inntekt større en 1,5G vil innfri minsteinntektskravet for aktør 12345
    Gitt at søker med aktør id "12345" med vedtak id 1234 med beregningsdato "2019-07-01"
    Når digidag skal vurdere minsteinntektkrav
    Så kravet til minsteinntekt er "oppfylt"
    Og har krav på 52 uker
