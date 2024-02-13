package no.nav.dagpenger.regel.api

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

private val LOGGER = KotlinLogging.logger {}

private val localProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "LOCAL",
        "application.httpPort" to "8093",
        "srvdp.regel.api.arena.adapter.username" to "username",
        "srvdp.regel.api.arena.adapter.password" to "password",
        "dp.regel.api.url" to "http://localhost/v1",
        "dp.regel.api.scope" to "api://dev-fss.teamdagpenger.dp-regel-api/.default",
        "dp.inntekt.api.url" to "http://localhost",
        "jwks.url" to "http://localhost",
        "jwks.issuer" to "http://localhost",
        "optional.jwt" to "true",
        "unleash.url" to "https://localhost",
    ),
)
private val devProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "DEV",
        "application.httpPort" to "8093",
        "dp.regel.api.url" to "http://dp-regel-api.teamdagpenger.svc.nais.local/v1",
        "dp.inntekt.api.url" to "https://dp-inntekt-api.intern.dev.nav.no",
        "dp.regel.api.scope" to "api://dev-fss.teamdagpenger.dp-regel-api/.default",
        "jwks.url" to "http://security-token-service.default.svc.nais.local/rest/v1/sts/jwks",
        "jwks.issuer" to "https://security-token-service.nais.preprod.local",
        "optional.jwt" to "false",
        "unleash.url" to "https://unleash.nais.preprod.local/api/",
    ),
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "PROD",
        "application.httpPort" to "8093",
        "dp.regel.api.url" to "http://dp-regel-api.teamdagpenger.svc.nais.local/v1",
        "dp.regel.api.scope" to "api://prod-fss.teamdagpenger.dp-regel-api/.default",
        "dp.inntekt.api.url" to "https://dp-inntekt-api.intern.nav.no",
        "jwks.url" to "http://security-token-service.default.svc.nais.local/rest/v1/sts/jwks",
        "jwks.issuer" to "https://security-token-service.nais.adeo.no",
        "optional.jwt" to "false",
        "unleash.url" to "https://unleash.nais.adeo.no/api/",
    ),
)

data class Configuration(
    val application: Application = Application(),
) {

    val tokenProvider: () -> String by lazy {
        {
            val azureAdConfig = OAuth2Config.AzureAd(config()).also {
                LOGGER.info { "Using Azure AD config: ${it.tokenEndpointUrl} and ${it.wellKnowUrl()}" }
            }
            val azureAdClient = CachedOauth2Client(
                tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
                authType = azureAdConfig.clientSecret(),
            )
            runBlocking { azureAdClient.clientCredentials(config()[Key("dp.regel.api.scope", stringType)]).accessToken }
        }
    }

    data class Application(
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = config()[Key("application.httpPort", intType)],
        val username: String = config()[Key("srvdp.regel.api.arena.adapter.username", stringType)],
        val password: String = config()[Key("srvdp.regel.api.arena.adapter.password", stringType)],
        val dpRegelApiUrl: String = config()[Key("dp.regel.api.url", stringType)],
        val dpInntektApiUrl: String = config()[Key("dp.inntekt.api.url", stringType)],
        val jwksUrl: String = config()[Key("jwks.url", stringType)],
        val jwksIssuer: String = config()[Key("jwks.issuer", stringType)],
        val optionalJwt: Boolean = config()[Key("optional.jwt", booleanType)],
        val unleashUrl: String = config()[Key("unleash.url", stringType)],

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

private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
    "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties
    else -> {
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}
