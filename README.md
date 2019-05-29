# Dagpenger Regel API - Arena Adapter

Adapter for Arena mot DigiDag Dagpenger regel API



## Starte applikasjonen lokalt

Se [Dagpenger docker compose](https://github.com/navikt/dagpenger/blob/master/docker-compose/README.md) og docker-compose

## Kjør "user acceptance tests" (Cucumber)

Etter å ha starte docker-compose i forrige avsnitt, kjør:

'./gradlew uatLocal'

## Kjøre mot testmiljø (preprod, eller dev)

dp-regel-api-arena-adapter er beskyttet med jwt autentisering, hent token med: 


Hent systembruker og passord fra fasit 
`DP_TOKEN=$(curl -v --user <systembruker> https://security-token-service.nais.preprod.local/rest/v1/sts/token/\?grant_type\=client_credentials\&scope\=openid | jq -r .access_token)`
(NB: installer jq hvis du ikke har det: `brew install jq` elns)

bruk token i request mot APIet:


`curl -v -H "Authorization: Bearer $DP_TOKEN" -X POST -H "Content-Type: application/json" https://dp-regel-api-arena-adapter.nais.preprod.local/v1/....`
`


## Working on Open API specification
### Install

1. Install [Node JS](https://nodejs.org/).
2. Clone repo and run `npm install` in the repo root.

### Usage

#### `npm start`
Starts the development server.

#### `npm run build`
Bundles the spec and prepares web_deploy folder with static assets.

#### `npm test`
Validates the spec.

#### `npm run gh-pages`
Deploys docs to GitHub Pages. You don't need to run it manually if you have Travis CI configured.
