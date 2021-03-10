package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.core.Method
import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import java.time.LocalDate

internal class RegelApiBehovHttpClient(private val httpClient: FuelHttpClient) {
    private val jsonAdapter = moshiInstance.adapter(BehovRequest::class.java)

    fun run(behovRequest: BehovRequest): String {

        val json = jsonAdapter.toJson(behovRequest)

        val (_, response, result) = httpClient.request(Method.POST, "/behov") {
            it.header("Content-Type" to "application/json")
            it.body(json)
        }.response()

        return result.fold(
            { response.headers["Location"].first() },
            {
                throw RegelApiBehovHttpClientException(
                    "Failed to run behov. Response message ${response.responseMessage}. Error message: ${it.message}. "
                )
            }
        )
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
    val regelverksdato: LocalDate = beregningsdato
) {
    val requestId: String = ulid.nextULID()
}

data class RegelKontekst(val id: String, val type: String = "vedtak")

class RegelApiBehovHttpClientException(override val message: String) : RuntimeException(message)
