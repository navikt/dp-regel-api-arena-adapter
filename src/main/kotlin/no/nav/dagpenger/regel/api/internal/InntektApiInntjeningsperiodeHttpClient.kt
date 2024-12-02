package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat

internal class InntektApiInntjeningsperiodeHttpClient(private val client: FuelHttpClient) {
    fun getInntjeningsperiode(parametere: InntjeningsperiodeParametre): InntjeningsperiodeResultat {
        val url = "/v1/samme-inntjeningsperiode"

        val (_, response, result) =
            client.post<InntjeningsPeriodeRespond>(url) { request ->
                request.header(mapOf("Content-Type" to "application/json"))
                request.body(
                    moshiInstance.adapter(InntjeningsPeriodeRequest::class.java)
                        .toJson(InntjeningsPeriodeRequest(parametere.beregningsdato, parametere.inntektsId)),
                )
            }

        return when (result) {
            is Result.Failure -> throw InntektApiInntjeningsperiodeHttpClientException(
                "Failed to return samme-inntjeningsperiode. " +
                    "Response message ${response.responseMessage}. Error message: ${result.error.message}",
            )

            is Result.Success -> result.get().let { InntjeningsperiodeResultat(it.sammeInntjeningsPeriode, parametere) }
        }
    }

    private data class InntjeningsPeriodeRequest(
        val beregningsdato: String,
        val inntektsId: String,
    )

    private data class InntjeningsPeriodeRespond(val sammeInntjeningsPeriode: Boolean)
}

class InntektApiInntjeningsperiodeHttpClientException(
    override val message: String,
) : RuntimeException(message)
