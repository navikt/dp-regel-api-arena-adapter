package no.nav.dagpenger.regel.api.internal

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.oidc.OidcClient
import no.nav.dagpenger.regel.api.internal.models.TaskStatus
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

internal class RegelApiTasksHttpClientTest {

    @Rule
    @JvmField
    var mockBackend = MockWebServer()

    @Test
    fun `Should honor timeout `() {
        val oidcClient: OidcClient = mockk(relaxed = true)
        val client = RegelApiTasksHttpClient(
            regelApiUrl = mockBackend.url("/").toString(),
            timeout = Duration.ZERO,
            oidcClient = oidcClient
        )
        assertThrows(
            RegelApiTimeoutException::class.java
        ) {
            runBlocking { client.pollTask("/") }
        }
    }

    @Test
    fun ` Should get response when task is task is done and redirected to the result  `() {
        val oidcClient: OidcClient = mockk(relaxed = true)
        mockBackend.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                return when (request?.path) {
                    "/task/123" -> MockResponse()
                        .setResponseCode(303)
                        .addHeader("Location", "/minsteinntekt/123")
                    else -> MockResponse()
                        .setResponseCode(404)
                }
            }
        }

        val client =
            RegelApiTasksHttpClient(regelApiUrl = mockBackend.url("").toString(), oidcClient = oidcClient)

        val response = runBlocking { client.pollTask("task/123") }
        assertEquals("/minsteinntekt/123", response.location)
    }

    @Test
    fun ` Should retry query until task have status DONE (and redirected) `() {
        val oidcClient: OidcClient = mockk(relaxed = true)
        mockBackend.enqueue(
            MockResponse()
                .setBody(responseBody(TaskStatus.PENDING))
                .addHeader("Location", "/task/123")
        )
        mockBackend.enqueue(
            MockResponse()
                .setBody(responseBody(TaskStatus.PENDING))
                .addHeader("Location", "/task/123")
        )
        mockBackend.enqueue(
            MockResponse()
                .setBody(responseBody(TaskStatus.DONE))
                .setResponseCode(303)
                .addHeader("Location", "/task/123")
        )
        val client =
            RegelApiTasksHttpClient(regelApiUrl = mockBackend.url("").toString(), oidcClient = oidcClient)

        val response = runBlocking { client.pollTask("task/123") }
        assertEquals(TaskStatus.DONE, response.task?.status)
    }

    fun responseBody(status: TaskStatus) = """

                {
                        "regel" : "MINSTEINNTEKT",
                        "status" : "${status.name}",
                        "expires" : "${LocalDateTime.now().plusMinutes(10)}"

                }

            """.trimIndent()
}