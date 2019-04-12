package no.nav.dagpenger.regel.api

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwk.SigningKeyNotFoundException
import java.net.URL
import java.util.concurrent.TimeUnit

class JwkProviders(private val jwkSetUrls: List<URL>) : JwkProvider {

    private val delegates = jwkSetUrls.map {
        JwkProviderBuilder(it).cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    }

    override fun get(keyId: String?): Jwk {
        val jwks = delegates.map {
            try {
                it.get(keyId)
            } catch (e: SigningKeyNotFoundException) {
                null
            }
        }
        return jwks.firstOrNull()
            ?: throw SigningKeyNotFoundException("Failed to get key with kid $keyId from jwks urls: $jwkSetUrls", null)
    }
}