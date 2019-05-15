package no.nav.dagpenger.regel.api

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

private val localProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "LOCAL",
        "application.httpPort" to "8093",
        "srvdp.regel.api.arena.adapter.username" to "username",
        "srvdp.regel.api.arena.adapter.password" to "password",
        "dp.regel.api.url" to "https://localhost",
        "dp.inntekt.api.url" to "https://localhost",
        "jwks.url" to "https://localhost",
        "jwks.issuer" to "https://localhost",
        "oidc.sts.issuerurl" to "https://localhost"
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "DEV",
        "application.httpPort" to "8093",
        "dp.regel.api.url" to "http://dp-regel-api",
        "dp.inntekt.api.url" to "http://dp-inntekt-api",
        "jwks.url" to "http://security-token-service/rest/v1/sts/jwks",
        "jwks.issuer" to "https://security-token-service.nais.preprod.local",
        "oidc.sts.issuerurl" to "https://security-token-service.nais.preprod.local/"
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "PROD",
        "application.httpPort" to "8093",
        "dp.regel.api.url" to "http://dp-regel-api",
        "dp.inntekt.api.url" to "http://dp-inntekt-api",
        "jwks.url" to "http://security-token-service/rest/v1/sts/jwks",
        "jwks.issuer" to "https://security-token-service.nais.adeo.no",
        "oidc.sts.issuerurl" to "https://security-token-service.nais.adeo.no/"
    )
)

data class Configuration(
    val application: Application = Application()
) {

    data class Application(
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = config()[Key("application.httpPort", intType)],
        val username: String = config()[Key("srvdp.regel.api.arena.adapter.username", stringType)],
        val password: String = config()[Key("srvdp.regel.api.arena.adapter.password", stringType)],
        val dpRegelApiUrl: String = config()[Key("dp.regel.api.url", stringType)],
        val dpInntektApiUrl: String = config()[Key("dp.inntekt.api.url", stringType)],
        val jwksUrl: String = config()[Key("jwks.url", stringType)],
        val jwksIssuer: String = config()[Key("jwks.issuer", stringType)],
        val oicdStsUrl: String = config()[Key("oidc.sts.issuerurl", stringType)]

    ) {
        init {
            LOGGER.info { "Using jwksurl $jwksUrl and issuer $jwksIssuer" }
        }
    }
}

enum class Profile {
    LOCAL, DEV, PROD
}

private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
    "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
    else -> {
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}