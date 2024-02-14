package no.nav.dagpenger.regel.api.internal

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RegelApiBehovHttpClientTest {
    companion object {
        val server: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun start() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            server.stop()
        }
    }

    @BeforeEach
    fun configure() {
        WireMock.configureFor(server.port())
    }

    @Test
    fun ` Should get url to behov status `() {
        val equalToPattern = EqualToPattern("regelApiKey")
        WireMock.stubFor(
            WireMock.post(WireMock.urlEqualTo("//behov"))
                .withHeader("X-API-KEY", equalToPattern)
                .withRequestBody(
                    EqualToJsonPattern(
                        """
                        {
                            "aktorId": "001",
                            "vedtakId": 123456,
                            "beregningsdato": "2019-04-14"
                        }
                        """.trimIndent(),
                        true,
                        true,
                    ),
                )
                .willReturn(
                    WireMock.aResponse()
                        .withBody(responseBody)
                        .withHeader("Location", "/behov/status/123"),
                ),
        )

        val client = RegelApiBehovHttpClient(FuelHttpClient(server.url(""), equalToPattern.value))

        val behovRequest =
            BehovRequest(
                "001",
                123456,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                LocalDate.of(2019, 4, 14),
            )

        val response = client.run(behovRequest)
        Assertions.assertEquals("/behov/status/123", response)
    }

    private val responseBody =
        """
        {
                "status" : "PENDING"
        }
        """.trimIndent()
}
