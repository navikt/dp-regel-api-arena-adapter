package no.nav.dagpenger.regel.api.arena.adapter.v1

import de.huxhorn.sulky.ulid.ULID
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.regel.api.JwtStub
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.mockedRegelApiAdapter
import no.nav.dagpenger.regel.api.internal.RegelApi
import no.nav.dagpenger.regel.api.internal.RegelApiMinsteinntektNyVurderingException
import no.nav.dagpenger.regel.api.serder.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate

class KreverRevurderingApiTest {
    private val jwkStub = JwtStub()
    private val token = jwkStub.createTokenFor("systembrukeren")

    private val kreverReberegningPath = "/v1/lovverk/vurdering/minsteinntekt"
    private val subsumsjonIder = listOf(ULID().nextULID(), ULID().nextULID())
    private val ukjentSubsumsjonId = ULID().nextULID()

    private val beregningsdato = LocalDate.of(2020, 1, 13)

    private val reberegningMockClient =
        mockk<RegelApi>().also {
            coEvery { it.kreverNyVurdering(subsumsjonIder = subsumsjonIder, beregningsdato) } returns true
            coEvery {
                it.kreverNyVurdering(subsumsjonIder = listOf(ukjentSubsumsjonId), beregningsdato)
            } throws RegelApiMinsteinntektNyVurderingException("Test exception")
        }

    @Test
    fun `Vurdering av minsteinntekt API specification test - Should match json field names and formats`() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = reberegningMockClient,
                )
            }
            val response =
                client.post(kreverReberegningPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                          "subsumsjonIder": [${subsumsjonIder.joinToString(prefix = "\"", separator = "\", \"", postfix = "\"")}],
                          "beregningsdato": "$beregningsdato"
                        }
                        """.trimIndent(),
                    )
                }
            response.status shouldBe HttpStatusCode.OK
            assertEquals("""{"reberegning": true}""", response.bodyAsText())
        }
    }

    @Test
    fun `Feil ved sjekk av krav om revurdeing av minsteinntekt `() {
        testApplication {
            application {
                mockedRegelApiAdapter(
                    jwkProvider = jwkStub.stubbedJwkProvider(),
                    regelApi = reberegningMockClient,
                )
            }
            val response =
                client.post(kreverReberegningPath) {
                    header(HttpHeaders.ContentType, "application/json")
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(
                        """
                        {
                          "subsumsjonIder": ["$ukjentSubsumsjonId"],
                          "beregningsdato": "$beregningsdato"
                        }
                        """.trimIndent(),
                    )
                }
            response.status shouldBe HttpStatusCode.InternalServerError
            with(jacksonObjectMapper.readValue(response.bodyAsText(), Problem::class.java)) {
                this.shouldNotBeNull()
                title shouldBe "Feil ved sjekk om minsteinntekt må revurderes"
                type shouldBe URI("urn:dp:error:revurdering:minsteinntekt")
                status shouldBe 500
            }
        }
    }
}
