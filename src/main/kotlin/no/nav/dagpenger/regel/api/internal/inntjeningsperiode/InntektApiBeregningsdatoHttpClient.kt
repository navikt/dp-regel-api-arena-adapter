package no.nav.dagpenger.regel.api.internal.inntjeningsperiode

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import java.time.LocalDate

class InntektApiBeregningsdatoHttpClient(private val inntektApiUrl: String) {

    fun getBeregningsdato(inntektsId: String): LocalDate {
        val url = "$inntektApiUrl/beregningsdato/$inntektsId"
        val jsonAdapter = moshiInstance.adapter(BeregningsdatoResponse::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw InntektApiBeregningsdatoHttpClientException(
                "Failed to return beregningsdato. Response message ${response.responseMessage}. Error message: ${result.error.message}")
            is Result.Success -> result.get().beregningsdato
        }
    }
}

data class BeregningsdatoResponse(
    val beregningsdato: LocalDate
)

class InntektApiBeregningsdatoHttpClientException(
    override val message: String
) : RuntimeException(message)