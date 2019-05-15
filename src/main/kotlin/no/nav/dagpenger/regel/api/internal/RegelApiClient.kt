package no.nav.dagpenger.regel.api.internal

import no.nav.dagpenger.oidc.OidcClient

abstract class RegelApiClient(val oidcClient: OidcClient) {
    fun getOidcToken(): String = oidcClient.oidcToken().access_token
}