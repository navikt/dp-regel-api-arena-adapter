package no.nav.dagpenger.regel.api.arena.adapter

data class Environment(
    val username: String = getEnvVar("SRVDP_REGEL_API_ARENA_ADAPTER_USERNAME"),
    val password: String = getEnvVar("SRVDP_REGEL_API_ARENA_ADAPTER_PASSWORD"),
    val oicdStsUrl: String = getEnvVar("OIDC_STS_ISSUERURL"),
    val dpRegelApiUrl: String = getEnvVar("DAGPENGER_REGEL_API_REST_URL"),
    val httpPort: Int = 8093
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
