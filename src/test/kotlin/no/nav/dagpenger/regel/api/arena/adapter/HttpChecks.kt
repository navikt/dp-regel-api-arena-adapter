package no.nav.dagpenger.regel.api.arena.adapter

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test

class NaisChecksTest {

    @Test
    fun `The application has alive endpoint `() {
        withTestApplication({ mockedRegelApiAdapter() }) {
            handleRequest(HttpMethod.Get, "/isAlive").run {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }

    @Test
    fun `The application has ready endpoint `() {
        withTestApplication({ mockedRegelApiAdapter() }) {
            handleRequest(HttpMethod.Get, "/isReady").run {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }
}

class MetricsTest {

    @Test
    fun `The application produces metrics`() {
        withTestApplication({ mockedRegelApiAdapter() }) {
            handleRequest(HttpMethod.Get, "/metrics").run {
                response.status() shouldBe HttpStatusCode.OK
                response.content shouldContain "jvm_"
            }
        }
    }
}
