package no.nav.dagpenger.regel.api.internal

import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.spyk
import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.internal.models.Faktum
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SynchronousSubsumsjonClientTest {
    @Test
    fun `Get subsumsjon synchronously`() {
        val behovHttpClient: RegelApi = spyk()

        coEvery {
            behovHttpClient.run(behovRequest = any())
        } returns "behov/status/123"

        coEvery {
            behovHttpClient.pollStatus("behov/status/123")
        } returns "subsumsjon/0987"

        coEvery {
            behovHttpClient.getSubsumsjon("subsumsjon/0987")
        } returns subsumsjon()

        val behovRequest =
            BehovRequest(
                aktorId = "1234",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 4, 14),
            )

        val testFunction = { subsumsjon: Subsumsjon, _: LocalDateTime, _: LocalDateTime -> subsumsjon.behovId }

        val behovId = runBlocking { behovHttpClient.getSubsumsjonSynchronously(behovRequest, testFunction) }

        assertEquals("565656", behovId)
    }

    @Test
    fun `Should get metrics from the client `() {
        val behovHttpClient: RegelApi = spyk()

        coEvery {
            behovHttpClient.run(behovRequest = any())
        } returns "behov/status/123"

        coEvery {
            behovHttpClient.pollStatus("behov/status/123")
        } returns "subsumsjon/0987"

        coEvery {
            behovHttpClient.getSubsumsjon("subsumsjon/0987")
        } returns subsumsjon()

        val behovRequest =
            BehovRequest(
                aktorId = "1234",
                vedtakId = 123,
                regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
                beregningsdato = LocalDate.of(2019, 4, 14),
            )

        val testFunction = { subsumsjon: Subsumsjon, _: LocalDateTime, _: LocalDateTime -> subsumsjon.behovId }

        val behovId = runBlocking { behovHttpClient.getSubsumsjonSynchronously(behovRequest, testFunction) }

        assertEquals("565656", behovId)

        val registry = CollectorRegistry.defaultRegistry

        registry
            .metricFamilySamples()
            .asSequence()
            .find { it.name == CLIENT_LATENCY_SECONDS_METRIC_NAME }
            ?.let { metric ->
                metric.samples[0].value shouldNotBe null
                metric.samples[0].value shouldBeGreaterThan 0.0
            }
    }

    private fun subsumsjon(): Subsumsjon =
        Subsumsjon(
            behovId = "565656",
            faktum =
                Faktum(
                    aktorId = "1234",
                    regelkontekst = RegelKontekst("12345", "vedtak"),
                    beregningsdato = LocalDate.of(2019, 4, 14),
                ),
            minsteinntektResultat = null,
            periodeResultat = null,
            grunnlagResultat = null,
            satsResultat = null,
            problem = null,
        )
}
