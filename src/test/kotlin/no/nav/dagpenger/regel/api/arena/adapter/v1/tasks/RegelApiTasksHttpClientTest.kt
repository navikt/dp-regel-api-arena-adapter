package no.nav.dagpenger.regel.api.arena.adapter.v1.tasks

import kotlinx.coroutines.runBlocking
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
        val client = RegelApiTasksHttpClient(regelApiUrl = mockBackend.url("/").toString(), timeout = Duration.ZERO)
        assertThrows(
            RegelApiTimeoutException::class.java
        ) {
            runBlocking { client.pollTask("/") }
        }
    }

    @Test
    fun ` Should get response when task is in status DONE  `() {
        mockBackend.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                return when (request?.path) {
                    "/task/123" -> MockResponse()
                        .setBody(responseBody(TaskStatus.DONE))
                        .addHeader("Location", "/task/123")
                    else -> MockResponse()
                        .setResponseCode(404)
                }
            }
        }

        val client = RegelApiTasksHttpClient(regelApiUrl = mockBackend.url("").toString())

        val response = runBlocking { client.pollTask("task/123") }
        assertEquals(TaskStatus.DONE, response.task?.status)
    }

    @Test
    fun ` Should retry query until task have status DONE `() {
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
                .addHeader("Location", "/task/123")
        )
        val client = RegelApiTasksHttpClient(regelApiUrl = mockBackend.url("").toString())

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