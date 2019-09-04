#language: no
Egenskap: Hent dagpengergrunnlag

  Scenario: Beregn grunnlag og sats gitt ingen inntekt
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Når digidag skal beregne grunnlag
    Så returneres en feil "Inntektene gir 0 eller negativ resultat i grunnlag"

  Scenario: Beregn grunnlag og sats gitt inntekt
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Når digidag skal beregne grunnlag
    Så er avkortet grunnlag satt til 180374 og uavkortet til 180374
    Og er ukessats satt til 2165
    Og dagsats satt til 433

  Scenario: Beregn grunnlag og sats gitt manuelt grunnlag
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og det er beregnet med et manuelt grunnlag på 600000
    Når digidag skal beregne grunnlag
    Så er avkortet grunnlag satt til 599148 og uavkortet til 600000
    Og er ukessats satt til 7190
    Og dagsats satt til 1438

  Scenario: Beregn grunnlag og sats gitt barnetillegg
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og søker har 9 barn
    Når digidag skal beregne grunnlag
    Så er avkortet grunnlag satt til 180374 og uavkortet til 180374
    Og er ukessats satt til 2930
    Og dagsats satt til 433
    Og da er parameteret barn 9
    Og da er parameteret brukt nitti prosent regelen for barn satt

  Scenario: Beregn grunnlag og sats gitt mange barn
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og søker har 100 barn
    Når digidag skal beregne grunnlag
    Så er avkortet grunnlag satt til 180374 og uavkortet til 180374
    Og er ukessats satt til 3122
    Og dagsats satt til 433
    Og da er parameteret barn 100
    Og da er parameteret brukt nitti prosent regelen er "true"

  Scenario: Verneplikt gir høyere grunnlag enn inntekt
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og søker har avtjent verneplikt
    Når digidag skal beregne grunnlag
    Så er avkortet grunnlag satt til 299574 og uavkortet til 299574
    Og er ukessats satt til 3595
    Og dagsats satt til 719
    Og benyttet beregningsregel "VERNEPLIKT"

  Scenario: Ingen inntekt men verneplikt
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og søker har avtjent verneplikt
    Når digidag skal beregne grunnlag
    Så er avkortet grunnlag satt til 299574 og uavkortet til 299574
    Og er ukessats satt til 3595
    Og dagsats satt til 719
    Og benyttet beregningsregel "VERNEPLIKT"

