package no.nav.dagpenger.regel.api.arena.adapter

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.regel.api.Profile

private val localProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "LOCAL",
        "dp.regel.api.arena.adapter.url" to "http://127.0.0.1:8093",
        "oidc.sts.issuerurl" to "https://vtpmock.local:8063/stsrest/rest/v1/sts/token",
        "cucumber.test.username" to "igroup",
        "cucumber.test.password" to "itest",
        "disable.ssl" to "true"

    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "application.profile" to "DEV",
        "dp.regel.api.arena.adapter.url" to "https://dp-regel-api-arena-adapter.nais.preprod.local",
        "oidc.sts.issuerurl" to "https://security-token-service.nais.preprod.local/rest/v1/sts/token",
        "disable.ssl" to "false"

    )
)

data class CucumberConfiguration(
    val dpApiArenaAdapterUrl: String = config()[Key("dp.regel.api.arena.adapter.url", stringType)],
    val stsIssuerUrl: String = config()[Key("oidc.sts.issuerurl", stringType)],
    val profile: Profile = config()[Key(
        "application.profile",
        stringType
    )].let { Profile.valueOf(it) },
    val username: String = config()[Key("cucumber.test.username", stringType)],
    val password: String = config()[Key("cucumber.test.password", stringType)],
    val disableSSL: Boolean = config()[Key("disable.ssl", booleanType)]
)

private fun config() = when (System.getenv("CUCUMBER_ENV") ?: System.getenv("CUCUMBER_ENV")) {
    "dev" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties
    else -> {
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties
    }
}