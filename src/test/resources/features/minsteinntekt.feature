#language: no
Egenskap: Vurderer minsteinntektskrav

  Scenario: For lav inntekt vil ikke innfri minsteinntektskravet
    Gitt at søker har inntekt under 1,5G siste år og under 3G siste 3 år
    Når digidag skal vurdere minsteinntektkrav
    Så blir ikke kravet innfridd

