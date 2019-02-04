#language: no
Egenskap: Sett minteinntekt, periode, grunnlag og sats

  Scenario: Ingen inntekt og avslag
    Gitt at søker har ingen inntekt siste 36 måneder
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "ikke oppfylt"

  Scenario: Ingen inntekt og verneplikt skal gi 26 uker
    Gitt at søker har ingen inntekt siste 36 måneder
    Og har avtjent verneplikt
    Når digidag skal vurdere søknaden
    Så er kravet til minsteinntekt "oppfylt"
    Og perioden er 26 uker
    Og grunnlaget er XXX
    Og satsen er XXX

  Scenario: Søker har EØS
    Gitt at grunnlag er satt manuelt til XXX
    Når digidag skal sette sats
    Så er ukessats satt til XXX
    Og dagsats satt til XXX

  Scenario: Barnetillegg skal legges til
    Gitt at søker har fått minsteinntekt oppfylt
    Og søker skal få barnetilleg
    Når digidag skal sette sats
    Så er det lagt til 17 * 5 * antall barn på ukessats

  Scenario: Søker har tjent mellom 1,5 og 2 G siste 12 måneder
    Gitt at søker har tjent 1,8G siste 12 måneder
    Når digidag skal vurdere søknaden
    Så er kravet til minsteinntekt "oppfylt"
    Og perioden er 52 uker
    Og grunnlaget er satt til XXX
    Og dagsatsen er satt til XXX
    Og ukessatsen er satt til XXX

  Scenario: Inntekt fra fangst og fisk skal tas med
    Gitt at søker skal ha medberegnet inntekt fra fangst og fisk
    Når digidag skal vurdere søknaden
    Så tas denne inntekten med

  Scenario: Tidligere brukt inntekt skal trekkes fra
    Gitt at noe av inntekten er benyttet fra før
    Når digidag skal vurdere søknaden
    Så skal denne trekkes fra