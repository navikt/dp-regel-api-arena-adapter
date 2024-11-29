package no.nav.dagpenger.regel.api.arena.adapter

import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test

class NaisChecksTest {
    @Test
    fun `The application has alive endpoint `() {
        testApplication {
            application {
                mockedRegelApiAdapter()
            }
            val response = client.get("/isAlive")
            response.status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `The application has ready endpoint `() {
        testApplication {
            application {
                mockedRegelApiAdapter()
            }
            val response = client.get("/isReady")
            response.status shouldBe HttpStatusCode.OK
        }
    }
}

class MetricsTest {
    @Test
    fun `The application produces metrics`() {
        testApplication {
            application {
                mockedRegelApiAdapter()
            }
            val response = client.get("/metrics")
            response.status shouldBe HttpStatusCode.OK
        }
    }
}
