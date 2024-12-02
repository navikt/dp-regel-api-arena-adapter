package no.nav.dagpenger.regel.api.internal

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.regel.api.arena.adapter.v1.SubsumsjonProblem
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon
import java.time.LocalDate
import java.time.LocalDateTime

interface RegelApi {
    suspend fun run(behovRequest: BehovRequest): String

    suspend fun kreverNyVurdering(
        subsumsjonIder: List<String>,
        beregningsdato: LocalDate,
    ): Boolean

    suspend fun pollStatus(statusUrl: String): String

    suspend fun getSubsumsjon(subsumsjonLocation: String): Subsumsjon

    suspend fun <T> getSubsumsjonSynchronously(
        behovRequest: BehovRequest,
        extractResult: (subsumsjon: Subsumsjon, opprettet: LocalDateTime, utfort: LocalDateTime) -> T,
    ): T {
        val opprettet = LocalDateTime.now()
        val totalTimer = clientLatencyStats.labels("total").startTimer()

        val createBehovTimer = clientLatencyStats.labels("create").startTimer()
        val statusUrl = run(behovRequest)
        createBehovTimer.observeDuration()

        val pollBehovTimer = clientLatencyStats.labels("poll_total").startTimer()
        val subsumsjonLocation = pollStatus(statusUrl)
        pollBehovTimer.observeDuration()

        val resultOfBehovTimer = clientLatencyStats.labels("result").startTimer()
        val subsumsjon = getSubsumsjon(subsumsjonLocation)
        resultOfBehovTimer.observeDuration()

        val utfort = LocalDateTime.now()
        totalTimer.observeDuration()

        return subsumsjon.problem?.let { throw SubsumsjonProblem(it) }
            ?: extractResult(subsumsjon, opprettet, utfort)
    }
}

private val ulid = ULID()

data class BehovRequest(
    val aktorId: String,
    val vedtakId: Int,
    val regelkontekst: RegelKontekst,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean? = null,
    val oppfyllerKravTilFangstOgFisk: Boolean? = null,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val manueltGrunnlag: Int? = null,
    val forrigeGrunnlag: Int? = null,
    val antallBarn: Int? = null,
    val inntektsId: String? = null,
    val lærling: Boolean? = null,
    val regelverksdato: LocalDate = beregningsdato,
) {
    val requestId: String = ulid.nextULID()
}

data class RegelKontekst(
    val id: String,
    val type: String = "vedtak",
)

data class KreverNyVurderingRespons(
    val nyVurdering: Boolean,
)

data class KreverNyVurderingParametre(
    val beregningsdato: LocalDate,
    val subsumsjonIder: List<String>,
)

class RegelApiMinsteinntektNyVurderingException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)

class RegelApiStatusHttpClientException(
    override val message: String,
    override val cause: Throwable,
) : RuntimeException(message, cause)

class RegelApiTimeoutException(
    override val message: String,
) : RuntimeException(message)

class RegelApiSubsumsjonHttpClientException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)

class RegelApiBehovHttpClientException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
