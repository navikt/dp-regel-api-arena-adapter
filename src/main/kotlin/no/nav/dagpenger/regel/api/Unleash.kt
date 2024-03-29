package no.nav.dagpenger.regel.api

import io.getunleash.DefaultUnleash
import io.getunleash.util.UnleashConfig

fun setupUnleash(unleashApiUrl: String): DefaultUnleash {
    val appName = "dp-regel-api-arena-adapter"
    val unleashconfig =
        UnleashConfig.builder()
            .appName(appName)
            .instanceId(appName)
            .unleashAPI(unleashApiUrl)
            .build()

    return DefaultUnleash(unleashconfig)
}
