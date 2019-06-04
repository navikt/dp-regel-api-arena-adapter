package no.nav.dagpenger.regel.api.internal

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.internal.models.Subsumsjon

class RegelApiSubsumsjonHttpClient(private val regelApiUrl: String) {

    fun getSubsumsjon(subsumsjonLocation: String): Subsumsjon {
        val url = "$regelApiUrl$subsumsjonLocation"

        val jsonAdapter = moshiInstance.adapter(Subsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }

        return when (result) {
            is Result.Failure -> throw RegelApiSubsumsjonHttpClientException(
                    "Failed to fetch subsumsjon. Response message: ${response.responseMessage}. Error message: ${result.error.message}")
            is Result.Success -> result.get()
        }
    }
}

class RegelApiSubsumsjonHttpClientException(override val message: String) : RuntimeException(message)