package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat

internal class InntektApiInntjeningsperiodeHttpClient(private val client: FuelHttpClient) {

    fun getInntjeningsperiode(parametere: InntjeningsperiodeParametre): InntjeningsperiodeResultat {
        val url = "/v1/is-samme-inntjeningsperiode"

        val (_, response, result) = client.post<InntjeningsperiodeResultat>(url) { request ->
            request.header(mapOf("Content-Type" to "application/json"))
            request.body(moshiInstance.adapter(InntjeningsperiodeParametre::class.java).toJson(parametere))
        }

        return when (result) {
            is Result.Failure -> throw InntektApiInntjeningsperiodeHttpClientException(
                "Failed to return is-samme-inntjeningsperiode. Response message ${response.responseMessage}. Error message: ${result.error.message}"
            )
            is Result.Success -> result.get()
        }
    }
}

class InntektApiInntjeningsperiodeHttpClientException(
    override val message: String
) : RuntimeException(message)
