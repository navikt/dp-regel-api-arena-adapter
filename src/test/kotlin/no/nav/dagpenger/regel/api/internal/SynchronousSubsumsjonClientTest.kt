package no.nav.dagpenger.regel.api.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.arena.adapter.Problem
import no.nav.dagpenger.regel.api.arena.adapter.v1.SubsumsjonProblem
import no.nav.dagpenger.regel.api.internal.models.Faktum
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SynchronousSubsumsjonClientTest {

    @Test
    fun `Get subsumsjon synchronously`() {
        val behovHttpClient: RegelApiBehovHttpClient = mockk()
        val statusHttpClient: RegelApiStatusHttpClient = mockk()
        val subsumsjonHttpClient: RegelApiSubsumsjonHttpClient = mockk()

        every {
            behovHttpClient.run(behovRequest = any())
        } returns "behov/status/123"

        coEvery {
            statusHttpClient.pollStatus("behov/status/123")
        } returns "subsumsjon/0987"

        every {
            subsumsjonHttpClient.getSubsumsjon("subsumsjon/0987")
        } returns subsumsjon()

        val synchronousSubsumsjonClient = SynchronousSubsumsjonClient(
            behovHttpClient,
            statusHttpClient,
            subsumsjonHttpClient
        )

        val behovRequest = BehovRequest(
            aktorId = "1234",
            vedtakId = 123,
            regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
            beregningsdato = LocalDate.of(2019, 4, 14)
        )

        val testFunction = { subsumsjon: Subsumsjon, _: LocalDateTime, _: LocalDateTime -> subsumsjon.behovId }

        val behovId = runBlocking { synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, testFunction) }

        assertEquals("565656", behovId)
    }

    @Test
    fun `Should get metrics from the client `() {
        val behovHttpClient: RegelApiBehovHttpClient = mockk()
        val statusHttpClient: RegelApiStatusHttpClient = mockk()
        val subsumsjonHttpClient: RegelApiSubsumsjonHttpClient = mockk()

        every {
            behovHttpClient.run(behovRequest = any())
        } returns "behov/status/123"

        coEvery {
            statusHttpClient.pollStatus("behov/status/123")
        } returns "subsumsjon/0987"

        every {
            subsumsjonHttpClient.getSubsumsjon("subsumsjon/0987")
        } returns subsumsjon()

        val synchronousSubsumsjonClient = SynchronousSubsumsjonClient(
            behovHttpClient,
            statusHttpClient,
            subsumsjonHttpClient
        )

        val behovRequest = BehovRequest(
            aktorId = "1234",
            vedtakId = 123,
            regelkontekst = RegelKontekst(id = "123", type = "vedtak"),
            beregningsdato = LocalDate.of(2019, 4, 14)
        )

        val testFunction = { subsumsjon: Subsumsjon, _: LocalDateTime, _: LocalDateTime -> subsumsjon.behovId }

        val behovId = runBlocking { synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, testFunction) }

        assertEquals("565656", behovId)

        val registry = CollectorRegistry.defaultRegistry

        registry.metricFamilySamples().asSequence().find { it.name == CLIENT_LATENCY_SECONDS_METRIC_NAME }?.let { metric ->
            metric.samples[0].value shouldNotBe null
            metric.samples[0].value shouldBeGreaterThan 0.0
        }
    }

    @Test
    fun `Exception is thrown if subsumsjon has problem`() {
        val problem = Problem(title = "problem")
        val apply = mockk<RegelApiSubsumsjonHttpClient>().apply {
            every { this@apply.getSubsumsjon(any()) } returns subsumsjon().copy(problem = problem)
        }
        val behovHttpClient = mockk<RegelApiBehovHttpClient>(relaxed = true).apply {
            every { this@apply.run(any()) } returns "string"
        }
        val statusHttpClient = mockk<RegelApiStatusHttpClient>(relaxed = true).apply {
            coEvery { this@apply.pollStatus(any()) } returns "string"
        }

        shouldThrow<SubsumsjonProblem> {
            runBlocking {
                SynchronousSubsumsjonClient(
                    behovHttpClient,
                    statusHttpClient,
                    apply
                ).getSubsumsjonSynchronously(mockk()) { subsumsjon, _, _ -> subsumsjon }
            }
        }.apply {
            this.problem shouldBe problem
        }
    }

    private fun subsumsjon(): Subsumsjon {
        return Subsumsjon(
            behovId = "565656",
            faktum = Faktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 4, 14)
            ),
            minsteinntektResultat = null,
            periodeResultat = null,
            grunnlagResultat = null,
            satsResultat = null,
            problem = null
        )
    }
}
