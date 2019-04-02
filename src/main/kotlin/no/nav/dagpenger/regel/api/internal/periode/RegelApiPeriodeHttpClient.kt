package no.nav.dagpenger.regel.api.internal.periode

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import no.nav.dagpenger.regel.api.arena.adapter.moshiInstance
import no.nav.dagpenger.regel.api.arena.adapter.v1.models.MinsteinntektOgPeriodeParametere
import no.nav.dagpenger.regel.api.internal.models.InntektsPeriode
import no.nav.dagpenger.regel.api.internal.models.PeriodeParametere
import no.nav.dagpenger.regel.api.internal.models.PeriodeSubsumsjon
import no.nav.dagpenger.regel.api.internal.models.TaskResponse

class RegelApiPeriodeHttpClient(private val regelApiUrl: String) {

    private val jsonAdapter = moshiInstance.adapter(PeriodeParametere::class.java)
    fun getPeriode(ressursUrl: String): PeriodeSubsumsjon {
        val url = "$regelApiUrl$ressursUrl"
        val jsonAdapter = moshiInstance.adapter(PeriodeSubsumsjon::class.java)

        val (_, response, result) =
            with(url.httpGet()) { responseObject(moshiDeserializerOf(jsonAdapter)) }
        return when (result) {
            is Result.Failure -> throw RegelApiPeriodeHttpClientException(
                "Failed to run periode. Response message ${response.responseMessage}. Error message: ${result.error.message}. Response: ${response.body().asString("application/json")}}"
            )
            is Result.Success -> result.get()
        }
    }

    fun startPeriodeSubsumsjon(payload: MinsteinntektOgPeriodeParametere): String {
        val url = "$regelApiUrl/periode"

        val internalParametere = PeriodeParametere(
            aktorId = payload.aktorId,
            vedtakId = payload.vedtakId,
            beregningsdato = payload.beregningsdato,
            harAvtjentVerneplikt = payload.harAvtjentVerneplikt,
            oppfyllerKravTilFangstOgFisk = payload.oppfyllerKravTilFangstOgFisk,
            bruktInntektsPeriode = payload.bruktInntektsPeriode?.let {
                InntektsPeriode(
                    førsteMåned = it.foersteMaaned,
                    sisteMåned = it.sisteMaaned
                )
            }
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
            is Result.Failure -> throw RegelApiPeriodeHttpClientException(
                "Failed to run periode. Response message ${response.responseMessage}. Error message: ${result.error.message}. Response: ${response.body().asString("application/json")}"
            )
            is Result.Success ->
                response.headers["Location"].first()
        }
    }
}

class RegelApiPeriodeHttpClientException(
    override val message: String
) : RuntimeException(message)