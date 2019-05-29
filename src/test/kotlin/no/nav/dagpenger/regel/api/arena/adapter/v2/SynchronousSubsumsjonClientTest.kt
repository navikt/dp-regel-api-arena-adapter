package no.nav.dagpenger.regel.api.arena.adapter.v2

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.regel.api.internalV2.BehovRequest
import no.nav.dagpenger.regel.api.internalV2.RegelApiBehovHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiStatusHttpClient
import no.nav.dagpenger.regel.api.internalV2.RegelApiSubsumsjonHttpClient
import no.nav.dagpenger.regel.api.internalV2.SynchronousSubsumsjonClient
import no.nav.dagpenger.regel.api.internalV2.models.Faktum
import no.nav.dagpenger.regel.api.internalV2.models.Subsumsjon
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

        every {
            runBlocking { statusHttpClient.pollStatus("behov/status/123") }
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
            beregningsdato = LocalDate.of(2019, 4, 14)
        )

        val testFunction = { subsumsjon: Subsumsjon, _: LocalDateTime, _: LocalDateTime -> subsumsjon.behovId }

        val behovId = synchronousSubsumsjonClient.getSubsumsjonSynchronously(behovRequest, testFunction)

        assertEquals("565656", behovId)
    }

    private fun subsumsjon(): Subsumsjon {
        return Subsumsjon(
            id = "",
            behovId = "565656",
            faktum = Faktum(
                aktorId = "1234",
                vedtakId = 123,
                beregningsdato = LocalDate.of(2019, 4, 14)
            ),
            minsteinntektResultat = null,
            periodeResultat = null,
            grunnlagResultat = null,
            satsResultat = null
        )
    }
}
