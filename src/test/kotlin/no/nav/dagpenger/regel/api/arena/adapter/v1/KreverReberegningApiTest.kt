package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.internal.RegelApiReberegningHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class KreverReberegningApiTest {

    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    private val kreverReberegningPath = "/v1/lovverk/krever-reberegning"
    private val subsumsjonIder = listOf(ULID().nextULID(), ULID().nextULID())

    private val beregningsdato = LocalDate.of(2020, 1, 13)

    @Test
    fun `KreverReberergning API specification test - Should match json field names and formats`() {
        val reberegningClient = mockk<RegelApiReberegningHttpClient>().also {
            every { it.kreverReberegning(subsumsjonIder = subsumsjonIder, beregningsdato) } returns true
        }

        withTestApplication({
            mockedRegelApiAdapter(
                jwkProvider = jwkStub.stubbedJwkProvider(),
                reberegningHttpClient = reberegningClient
            )
        }) {
            handleRequest(HttpMethod.Post, kreverReberegningPath) {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    """
                    {
                      "subsumsjonIder": [${subsumsjonIder.joinToString(prefix = "\"", separator = "\", \"", postfix = "\"")}],
                      "beregningsdato": "$beregningsdato"
                    }
                    """.trimIndent().also { println(it) }
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("""{"reberegning": true}""", response.content)
            }
        }
    }
}
