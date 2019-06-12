#language: no
Egenskap: Hent dagpengergrunnlag

  Scenario: Beregn grunnlag og sats gitt ingen inntekt
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Når digidag skal beregne grunnlag
    Så er grunnlag satt til avkortet satt til 0 og uavkortet til 0
    Og er ukessats satt til 0
    Og dagsats satt til 0

  Scenario: Beregn grunnlag og sats gitt inntekt
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Når digidag skal beregne grunnlag
    Så er grunnlag satt til avkortet satt til 180374 og uavkortet til 180374
    Og er ukessats satt til 2165
    Og dagsats satt til 433

  Scenario: Beregn grunnlag og sats gitt manuelt grunnlag
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og det er beregnet med et manuelt grunnlag på 600000
    Når digidag skal beregne grunnlag
    Så er grunnlag satt til avkortet satt til 599148 og uavkortet til 600000
    Og er ukessats satt til 7190
    Og dagsats satt til 1438

  Scenario: Beregn grunnlag og sats gitt barnetillegg
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og søker har 9 barn
    Når digidag skal beregne grunnlag
    Så er grunnlag satt til avkortet satt til 180374 og uavkortet til 180374
    Og er ukessats satt til 2930
    Og dagsats satt til 433
    Og da er parameteret barn 9