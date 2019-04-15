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
    Så er grunnlag satt til avkortet satt til 175000 og uavkortet til 175000
    Og er ukessats satt til 2100
    Og dagsats satt til 420

  Scenario: Beregn grunnlag og sats gitt manuelt grunnlag
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og det er beregnet med et manuelt grunnlag på 600000
    Når digidag skal beregne grunnlag
    Så er grunnlag satt til avkortet satt til 581298 og uavkortet til 600000
    Og er ukessats satt til 6976
    Og dagsats satt til 1395

  Scenario: Beregn grunnlag og sats gitt barnetillegg
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01" i beregning av grunnlag
    Og søker har 9 barn
    Når digidag skal beregne grunnlag
    Så er grunnlag satt til avkortet satt til 175000 og uavkortet til 175000
    Og er ukessats satt til 2865
    Og dagsats satt til 420