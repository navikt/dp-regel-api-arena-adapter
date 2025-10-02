package no.nav.dagpenger.regel.api

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

private val LOGGER = KotlinLogging.logger {}

private val localProperties =
    ConfigurationMap(
        mapOf(
            "dp.regel.api.url" to "http://localhost/v1",
            "dp.regel.api.scope" to "api://dev-gcp.teamdagpenger.dp-regel-api/.default",
            "dp.inntekt.api.url" to "http://localhost",
            "jwks.url" to "http://localhost",
            "jwks.issuer" to "http://localhost",
            "optional.jwt" to "true",
        ),
    )
private val devProperties =
    ConfigurationMap(
        mapOf(
            "dp.regel.api.url" to "https://dp-regel-api.intern.dev.nav.no",
            "dp.inntekt.api.url" to "https://dp-inntekt-api.intern.dev.nav.no",
            "dp.regel.api.scope" to "api://dev-gcp.teamdagpenger.dp-regel-api/.default",
            "jwks.url" to "http://security-token-service.default.svc.nais.local/rest/v1/sts/jwks",
            "jwks.issuer" to "https://security-token-service.nais.preprod.local",
            "optional.jwt" to "false",
        ),
    )
private val prodProperties =
    ConfigurationMap(
        mapOf(
            "dp.regel.api.url" to "https://dp-regel-api.intern.nav.no",
            "dp.regel.api.scope" to "api://prod-gcp.teamdagpenger.dp-regel-api/.default",
            "dp.inntekt.api.url" to "https://dp-inntekt-api.intern.nav.no",
            "jwks.url" to "http://security-token-service.default.svc.nais.local/rest/v1/sts/jwks",
            "jwks.issuer" to "https://security-token-service.nais.adeo.no",
            "optional.jwt" to "false",
        ),
    )

data class Configuration(
    val application: Application = Application(),
) {
    private val azureAdConfig by lazy { OAuth2Config.AzureAd(config()) }
    private val azureAdClient by lazy {
        CachedOauth2Client(
            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
            authType = azureAdConfig.clientSecret(),
        )
    }

    val tokenProvider: () -> String by lazy {
        {
            runBlocking {
                azureAdClient
                    .clientCredentials(config()[Key("dp.regel.api.scope", stringType)])
                    .access_token ?: throw RuntimeException("Failed to get token")
            }
        }
    }

    data class Application(
        val httpPort: Int = 8093,
        val dpRegelApiBaseUrl: String = config()[Key("dp.regel.api.url", stringType)],
        val dpInntektApiUrl: String = config()[Key("dp.inntekt.api.url", stringType)],
        val jwksUrl: String = config()[Key("jwks.url", stringType)],
        val jwksIssuer: String = config()[Key("jwks.issuer", stringType)],
        val optionalJwt: Boolean = config()[Key("optional.jwt", booleanType)],
    ) {
        init {
            LOGGER.info { "Using jwksurl $jwksUrl and issuer $jwksIssuer" }
        }
    }
}

enum class Profile {
    LOCAL,
    DEV,
    PROD,
}

private fun config() =
    when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
        "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
        else -> {
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
        }
    }
