#language: no
Egenskap: Vurderer minsteinntektskrav

  Scenario: Ingen inntekt og avslag
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "ikke oppfylt"
    Og antall uker er ikke satt

  Scenario: Ingen inntekt og verneplikt skal gi 26 uker
    Gitt at søker med aktør id "INGEN_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Og har avtjent verneplikt
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "oppfylt"
    Og har krav på 26 uker

  Scenario: Inntekt større en 1,5G og mindre enn 2G siste 12 måneder vil innfri minsteinntektskravet og gi 52
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "oppfylt"
    Og har krav på 52 uker

  Scenario: Inntekt større enn eller lik 2G siste 36 måneder vil innfri minsteinntektskravet og gi 104 uker
    Gitt at søker med aktør id "3_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "oppfylt"
    Og har krav på 104 uker

  Scenario: Inntekt fra fangst og fisk skal kun tas med hvis parameter for fangs og fisk er satt
    Gitt at søker med aktør id "FF_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "ikke oppfylt"
    Og inntektene inneholder fangs og fisk

  Scenario: Inntekt fra fangst og fisk skal kun tas med hvis saksbehandler vurderer det
    Gitt at søker med aktør id "FF_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Og at søker skal ha medberegnet inntekt fra fangst og fisk
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "oppfylt"

  Scenario: Tidligere brukt inntekt skal trekkes fra
    Gitt at søker med aktør id "1.5_G_INNTEKT" med vedtak id 12345 med beregningsdato "2019-07-01"
    Og hvor brukt inntekt er fra førstemåned "2018-06" og sistemåned "2019-06"
    Når digidag skal vurdere minsteinntektkrav og periode
    Så kravet til minsteinntekt er "ikke oppfylt"
    Og antall uker er ikke satt
    Og parameteret inneholder bruktInntektsPeriode