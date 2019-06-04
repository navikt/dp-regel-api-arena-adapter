package no.nav.dagpenger.regel.api.internal.inntjeningsperiode

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeParametre
import no.nav.dagpenger.regel.api.internal.models.InntjeningsperiodeResultat

class InntektApiInntjeningsperiodeHttpClient(private val inntektApiUrl: String) {

    fun getInntjeningsperiode(parametere: InntjeningsperiodeParametre): InntjeningsperiodeResultat {
        val url = "$inntektApiUrl/v1/is-samme-inntjeningsperiode"

        val jsonAdapterResultat = moshiInstance.adapter(InntjeningsperiodeResultat::class.java)
        val jsonAdapterParametere = moshiInstance.adapter(InntjeningsperiodeParametre::class.java)

        val jsonBody = jsonAdapterParametere.toJson(parametere)

        val (_, response, result) =
            with(url.httpPost()) {
                header(mapOf("Content-Type" to "application/json"))
                body(jsonBody)
                responseObject(moshiDeserializerOf(jsonAdapterResultat))
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