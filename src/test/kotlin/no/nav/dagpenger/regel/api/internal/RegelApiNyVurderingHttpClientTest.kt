package no.nav.dagpenger.regel.api.internal

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import io.kotest.common.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class RegelApiNyVurderingHttpClientTest {
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
    fun `Should get minsteinntekt vurdering`() =
        runBlocking {
            val tokenProvider = { "Token" }
            WireMock.stubFor(
                WireMock
                    .post(WireMock.urlEqualTo("/lovverk/vurdering/minsteinntekt"))
                    .withHeader("Authorization", EqualToPattern("Bearer ${tokenProvider.invoke()}"))
                    .withHeader("Accept", EqualToPattern("application/json"))
                    .willReturn(
                        WireMock
                            .aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"nyVurdering": true}"""),
                    ),
            )

            val client = RegelApiBehovHttpClient(server.url(""), tokenProvider)
            val response = client.kreverNyVurdering(listOf("123"), LocalDate.of(2020, 1, 13))
            assertTrue(response)
        }
}
