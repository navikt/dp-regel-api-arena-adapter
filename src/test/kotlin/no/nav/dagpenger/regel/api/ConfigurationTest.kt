package no.nav.dagpenger.regel.api

import io.kotlintest.shouldBe
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
                this.application.profile shouldBe Profile.DEV
                this.application.jwksIssuer shouldBe "https://security-token-service.nais.preprod.local"
                this.application.jwksUrl shouldBe "https://security-token-service.nais.preprod.local/rest/v1/sts/jwks"
                this.application.secondjwksUrl shouldBe "https://security-token-service-t10.nais.preprod.local/rest/v1/sts/jwks"
                this.application.dpInntektApiUrl shouldBe "http://dp-inntekt-api"
                this.application.dpRegelApiUrl shouldBe "http://dp-regel-api"
            }
        }

        withProps(dummyConfigs + mapOf("NAIS_CLUSTER_NAME" to "prod-fss")) {
            with(Configuration()) {
                this.application.profile shouldBe Profile.PROD
                this.application.jwksIssuer shouldBe "https://security-token-service.nais.adeo.no"
                this.application.jwksUrl shouldBe "http://security-token-service/rest/v1/sts/jwks"
                this.application.secondjwksUrl shouldBe null
                this.application.dpInntektApiUrl shouldBe "http://dp-inntekt-api"
                this.application.dpRegelApiUrl shouldBe "http://dp-regel-api"
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