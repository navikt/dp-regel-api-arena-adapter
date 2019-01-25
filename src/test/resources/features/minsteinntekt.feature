#language: no
Egenskap: Vurderer minsteinntektskrav

  Scenario: For lav inntekt vil ikke innfri minsteinntektskravet
    Gitt at søker har inntekt under 1,5G
    Når digidag skal vurdere minsteinntektkrav
    Så blir ikke kravet innfridd

