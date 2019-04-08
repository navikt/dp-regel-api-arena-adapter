package no.nav.dagpenger.regel.api

import org.junit.jupiter.api.Test

internal class ConfigurationTest {
    @Test
    fun `Default configuration is LOCAL `() {
        with(Configuration()) {
            kotlin.test.assertEquals(Profile.LOCAL, this.application.profile)
        }
    }

    private val dummyConfigs = listOf(
        "srvdp.regel.api.arena.adapter.username",
        "srvdp.regel.api.arena.adapter.password",
        "oidc.sts.issuerurl"
    )

    @Test
    fun `Configuration is loaded based on application profile`() {
        val dummyConfigs = dummyConfigs.associate { it to "test" }
        withProps(dummyConfigs + mapOf("NAIS_CLUSTER_NAME" to "dev-fss")) {
            with(Configuration()) {
                kotlin.test.assertEquals(Profile.DEV, this.application.profile)
            }
        }

        withProps(dummyConfigs + mapOf("NAIS_CLUSTER_NAME" to "prod-fss")) {
            with(Configuration()) {
                kotlin.test.assertEquals(Profile.PROD, this.application.profile)
            }
        }
    }

    private fun withProps(props: Map<String, String>, test: () -> Unit) {
        for ((k, v) in props) {
            System.getProperties()[k] = v
        }
        test()
        for ((k, _) in props) {
            System.getProperties().remove(k)
        }
    }
}