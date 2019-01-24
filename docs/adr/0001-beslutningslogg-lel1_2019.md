# Beslutningslogg for LEL1 2019

* Status: Pending
* Deciders: Team digitale dagpenger
* Date: 2019-01-24


## Context and Problem Statement

LEL1 2019 API (regelapi) mot Arena


## Decision Outcome

Logg 2019-01-23:
Endringer:
- Vi fjerner (i første runde gjør den optional) `inntektsId` i `dp-regel-api-arena-adapter`.
- Vi legger til `beregningsId` på grunnlag.


### Consequences


Logg 2019-01-23:
Konsekvenser:
- Minsteinntekt henter alltid "siste gjeldende inntekt" fra `dp-inntekts-api` hver gang den kjøres.
- Grunnlag henter alltid "siste gjeldende inntekt", med mindre `beregningsId` settes.
- Er `beregningsId` satt er det kun parameterene verneplikt, fangst og fisk og antall barn som kan endres - *ikke* beregningsdato.
- Er `beregningsId` satt så henter vi opp den beregningen, endrer parameterene og kjører regelen på nytt.
- Er `beregningsId` satt så får alltid Arena svar med en ny `beregningsId`

### Positive Consequences 

* _ 
### Negative consequences
* …
