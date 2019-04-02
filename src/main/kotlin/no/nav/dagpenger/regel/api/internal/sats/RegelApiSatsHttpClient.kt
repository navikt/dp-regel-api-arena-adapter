package no.nav.dagpenger.regel.api.internal.sats

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.GrunnlagOgSatsParametere
import no.nav.dagpenger.regel.api.internal.models.SatsParametere
import no.nav.dagpenger.regel.api.internal.models.SatsSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.TaskResponse

class RegelApiSatsHttpClient(private val regelApiUrl: String) {

    private val jsonAdapter = moshiInstance.adapter(SatsParametere::class.java)
    fun startSatsSubsumsjon(payload: GrunnlagOgSatsParametere): String {
        val url = "$regelApiUrl/sats"

        val internalParametere = SatsParametere(
            aktorId = payload.aktorId,
            vedtakId = payload.vedtakId,
            beregningsdato = payload.beregningsdato,
            antallBarn = payload.antallBarn,
            manueltGrunnlag = payload.grunnlag
        )

        val json = jsonAdapter.toJson(internalParametere)

        val (_, response, result) =
            with(
                url.httpPost()
                    .header(mapOf("Content-Type" to "application/json"))
                    .body(json)
            ) {
                responseObject<TaskResponse>()
            }
        return when (result) {
            is Result.Failure -> throw RegelApiSatsHttpClientException(
                "Failed to run sats. Response message ${response.responseMessage}. Error message: ${result.error.message}. Response: ${response.data}")
            is Result.Success ->
                response.headers["Location"].first()
        }
    }

    fun getSats(ressursUrl: String): SatsSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(SatsSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiSatsHttpClientException(
                "Failed to run sats. Response message ${response.responseMessage}. Error message: ${result.error.message}. Response: ${response.data}")
            is Result.Success -> result.get()
        }
    }
}

class RegelApiSatsHttpClientException(
    override val message: String
) : RuntimeException(message)