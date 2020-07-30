package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.httpPost
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.responseObject
import no.nav.dagpenger.regel.api.internal.models.BehovStatusResponse
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import java.time.LocalDate

class RegelApiBehovHttpClient(private val regelApiUrl: String, private val regelApiKey: String) {
    private val jsonAdapter = moshiInstance.adapter(BehovRequest::class.java)

    fun run(behovRequest: BehovRequest): String {
        val behovUrl = "$regelApiUrl/behov"

        val json = jsonAdapter.toJson(behovRequest)

        val (_, response, result) =
            with(
                behovUrl.httpPost()
                    .apiKey(regelApiKey)
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)
            ) {
                responseObject<BehovStatusResponse>()
            }

        return result.fold(
            { response.headers["Location"].first() },
            {
                throw RegelApiBehovHttpClientException(
                    "Failed to run behov. Response message ${response.responseMessage}. Error message: ${it.message}"
                )
            }
        )
    }
}

data class BehovRequest(
    val aktorId: String,
    val vedtakId: Int,
    val beregningsdato: LocalDate,
    val harAvtjentVerneplikt: Boolean? = null,
    val oppfyllerKravTilFangstOgFisk: Boolean? = null,
    val bruktInntektsPeriode: InntektsPeriode? = null,
    val manueltGrunnlag: Int? = null,
    val antallBarn: Int? = null,
    val inntektsId: String? = null,
    val lærling: Boolean? = null
)

class RegelApiBehovHttpClientException(override val message: String) : RuntimeException(message)
