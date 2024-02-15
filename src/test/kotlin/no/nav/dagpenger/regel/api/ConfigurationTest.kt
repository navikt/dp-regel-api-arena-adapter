package no.nav.dagpenger.regel.api

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ConfigurationTest {
    @Test
    fun `Configuration is loaded based on application profile`() {
        withProps(mapOf("NAIS_CLUSTER_NAME" to "prod-fss")) {
            with(Configuration()) {
                this.application.jwksIssuer shouldBe "https://security-token-service.nais.adeo.no"
                this.application.jwksUrl shouldBe "http://security-token-service.default.svc.nais.local/rest/v1/sts/jwks"
                this.application.dpInntektApiUrl shouldBe "https://dp-inntekt-api.intern.nav.no"
                this.application.dpRegelApiBaseUrl shouldBe "https://dp-regel-api.intern.nav.no"
            }
        }
    }

    private fun withProps(
        props: Map<String, String>,
        test: () -> Unit,
    ) {
        for ((k, v) in props) {
            System.getProperties()[k] = v
        }
        try {
            test()
        } finally {
            for ((k, _) in props) {
                System.getProperties().remove(k)
            }
        }
    }
}
